package com.ssgpack.ssgfc.vote;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.util.UtilUpload;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteTitleRepository voteTitleRepository;
    private final VoteContentRepository voteContentRepository;
    private final UserVoteRepository userVoteRepository;
    private final UtilUpload utilUpload; // ✅ 이미지 업로드 유틸

    // ✅ 전체 투표 목록 조회 (최신순 정렬)
    public List<VoteTitle> getAllVotes() {
        return voteTitleRepository.findAllByOrderByCreateDateDesc();
    }

    // ✅ 특정 투표(VoteTitle)의 ID로 연결된 선택지(VoteContent) 리스트 조회
    public List<VoteContent> getContentsByTitleId(Long voteTitleId) {
        VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투표가 존재하지 않습니다."));
        return voteContentRepository.findByVoteTitle(voteTitle);
    }

    // ✅ 최신 투표 1개 조회 (생성일 기준 최신)
    public VoteTitle getLatestVote() {
        return voteTitleRepository.findTopByOrderByCreateDateDesc();
    }

    // ✅ 특정 투표(VoteTitle)의 선택지별 득표 수 및 비율 계산
    public Map<String, String> getVoteResult(VoteTitle voteTitle) {
        List<VoteContent> contents = voteContentRepository.findByVoteTitle(voteTitle);
        Map<String, String> result = new LinkedHashMap<>();

        long totalVotes = contents.stream()
                .mapToLong(userVoteRepository::countByVoteContent)
                .sum();

        for (VoteContent content : contents) {
            long count = userVoteRepository.countByVoteContent(content);
            String percentage = (totalVotes > 0)
                    ? String.format("%.0f%%", (count * 100.0) / totalVotes)
                    : "0%";
            result.put(content.getContent(), count + "표 (" + percentage + ")");
        }

        return result;
    }

    // ✅ 사용자가 투표에 참여할 때 호출 (중복 참여 방지)
    public void submitVote(User user, Long voteTitleId, Long voteContentId) {
        if (user == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        if (voteContentId == null) {
            throw new IllegalArgumentException("선택지를 선택해주세요.");
        }

        VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId)
                .orElseThrow(() -> new IllegalArgumentException("투표가 존재하지 않습니다."));
        
        // ✅ 마감일 체크
        if (voteTitle.getEndDate() != null && voteTitle.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("마감된 투표입니다.");
        }

        VoteContent voteContent = voteContentRepository.findById(voteContentId)
                .orElseThrow(() -> new IllegalArgumentException("선택지가 존재하지 않습니다."));

        userVoteRepository.findByUserIdAndVoteTitleId(user.getId(), voteTitleId)
                .ifPresent(existing -> {
                    throw new IllegalStateException("이미 투표에 참여하셨습니다.");
                });

        UserVote userVote = UserVote.builder()
                .user(user)
                .voteTitle(voteTitle)
                .voteContent(voteContent)
                .createDate(LocalDateTime.now())
                .build();

        userVoteRepository.save(userVote);
    }

    // ✅ 투표 생성 처리 (제목, 이미지 업로드, 선택지 저장)
    public void insertVote(VoteForm form, MultipartFile file) {
        VoteTitle voteTitle = VoteTitle.builder()
                .title(form.getTitle())
                .createDate(LocalDateTime.now())
                .endDate(form.getEndDate()) // ✅ 마감일 추가
                .build();

        if (file != null && !file.isEmpty()) {
            try {
                String savedName = utilUpload.fileUpload(file, "vote/");
                voteTitle.setImg(savedName);
            } catch (Exception e) {
                e.printStackTrace();
                voteTitle.setImg("default.png");
            }
        }

        voteTitle = voteTitleRepository.save(voteTitle);

        // ✅ 여기 수정 (null 체크 추가)
        if (form.getContents() != null) {
            for (String content : form.getContents()) {
                if (content != null && !content.isBlank()) {
                    VoteContent voteContent = VoteContent.builder()
                            .content(content)
                            .voteTitle(voteTitle)
                            .build();
                    voteContentRepository.save(voteContent);
                }
            }
        }
    }

    // ✅ 특정 선택지(VoteContent)의 총 투표 수 반환
    public long getVoteCount(VoteContent content) {
        return userVoteRepository.countByVoteContent(content);
    }

    // ✅ 특정 투표(VoteTitle) 삭제
    public void deleteVote(Long voteTitleId) {
        VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투표가 존재하지 않습니다."));
        voteTitleRepository.delete(voteTitle);
    }

    // ✅ 기존 VoteTitle을 수정폼(VoteForm)으로 변환 (수정 페이지용)
    public VoteForm convertToForm(VoteTitle voteTitle) {
        VoteForm form = new VoteForm();
        form.setTitle(voteTitle.getTitle());
        form.setEndDate(voteTitle.getEndDate());

        List<String> contents = voteTitle.getContents().stream()
                .map(VoteContent::getContent)
                .collect(Collectors.toList());
        form.setContents(contents);

        return form;
    }

    // ✅ 투표 수정 처리 (제목, 선택지, 이미지, 마감일 변경)
    public void updateVote(Long id, VoteForm form, MultipartFile file) {
        VoteTitle voteTitle = voteTitleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("투표가 존재하지 않습니다."));

        // ✅ 마감일 체크
        if (voteTitle.getEndDate() != null && voteTitle.getEndDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("마감된 투표는 수정할 수 없습니다.");
        }

        // ✅ 투표 제목과 마감일 수정
        voteTitle.setTitle(form.getTitle());
        voteTitle.setEndDate(form.getEndDate());

        if (file != null && !file.isEmpty()) {
            try {
                String savedName = utilUpload.fileUpload(file, "vote/");
                voteTitle.setImg(savedName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // ✅ 기존 선택지 삭제하지 않고, 새로운 선택지 이름만 수정
        for (int i = 0; i < form.getContents().size(); i++) {
            String content = form.getContents().get(i);
            if (!content.isBlank()) {
                VoteContent voteContent = voteContentRepository.findById(voteTitle.getContents().get(i).getId())
                        .orElseThrow(() -> new IllegalArgumentException("선택지가 존재하지 않습니다."));
                voteContent.setContent(content);  // 이름만 수정
                voteContentRepository.save(voteContent); // 수정된 선택지 저장
            }
        }

        // ✅ 수정된 투표와 선택지를 저장
        voteTitleRepository.save(voteTitle);
    }
    // ✅ 사용자가 특정 투표에서 선택한 선택지(VoteContent)의 ID를 반환 (없으면 null 반환)
    public Long getUserSelectedContentId(Long userId, Long voteTitleId) {
        return userVoteRepository.findByUserIdAndVoteTitleId(userId, voteTitleId)
                .map(userVote -> userVote.getVoteContent().getId())
                .orElse(null);
    }
    
}
