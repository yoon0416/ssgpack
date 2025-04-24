package com.ssgpack.ssgfc.vote;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private final UtilUpload utilUpload; // ✅ 이미지 업로드 유틸 주입

    // ✅ 전체 투표 목록을 최신순으로 정렬해 반환합니다.
    public List<VoteTitle> getAllVotes() {
        return voteTitleRepository.findAllByOrderByCreateDateDesc();
    }

    // ✅ 특정 투표(VoteTitle)의 ID를 기반으로 연결된 선택지(VoteContent) 목록을 조회하는 메서드입니다.
    public List<VoteContent> getContentsByTitleId(Long voteTitleId) {
        VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId).orElseThrow();
        return voteContentRepository.findByVoteTitle(voteTitle);
    }

    // ✅ 최신 투표 1개 반환 (JPA 메서드로 간단화)
    public VoteTitle getLatestVote() {
        return voteTitleRepository.findTopByOrderByCreateDateDesc();
    }

    // ✅ 투표 결과를 구성하여 선택지별 득표 수 및 퍼센트를 반환합니다.
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

    // ✅ 사용자가 투표에 참여할 때 호출됩니다. 중복 여부 확인 및 투표 저장 처리.
    public void submitVote(User user, Long voteTitleId, Long voteContentId) {
        if (user == null) throw new IllegalArgumentException("로그인이 필요합니다.");
        if (voteContentId == null) throw new IllegalArgumentException("선택지를 선택해주세요.");

        VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId)
                .orElseThrow(() -> new IllegalArgumentException("투표가 존재하지 않습니다."));

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

	// ✅ 투표 생성 처리 (선택지 + 이미지 업로드 포함)
	public void insertVote(VoteForm form, MultipartFile file) {
		VoteTitle voteTitle = VoteTitle.builder().title(form.getTitle()).createDate(LocalDateTime.now()).build();

		// ✅ 이미지 파일 저장 처리 (try-catch 추가)
		if (file != null && !file.isEmpty()) {
			try {
				String savedName = utilUpload.fileUpload(file, "vote/");
				voteTitle.setImg(savedName);
			} catch (Exception e) {
				e.printStackTrace(); // ✅ 콘솔에 오류 로그 출력
				// 상황에 따라 기본 이미지 지정도 가능
				voteTitle.setImg("default.png"); // 예: 기본 이미지 대체
			}
		}

		voteTitle = voteTitleRepository.save(voteTitle);

		// ✅ 선택지 저장
		for (String content : form.getContents()) {
			if (!content.isBlank()) {
				VoteContent voteContent = VoteContent.builder().content(content).voteTitle(voteTitle).build();
				voteContentRepository.save(voteContent);
			}
		}
	}


    // ✅ 특정 선택지의 총 투표 수를 반환합니다.
    public long getVoteCount(VoteContent content) {
        return userVoteRepository.countByVoteContent(content);
    }

    // ✅ 특정 투표를 삭제합니다.
    public void deleteVote(Long voteTitleId) {
        VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 투표가 존재하지 않습니다."));
        voteTitleRepository.delete(voteTitle);
    }

}
