package com.ssgpack.ssgfc.vote;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ssgpack.ssgfc.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteTitleRepository voteTitleRepository;
    private final VoteContentRepository voteContentRepository;
    private final UserVoteRepository userVoteRepository;

    public List<VoteTitle> getAllVotes() {
        return voteTitleRepository.findAll();
    }

    public List<VoteContent> getContentsByTitleId(Long voteTitleId) {
        VoteTitle voteTitle = voteTitleRepository.findById(voteTitleId).orElseThrow();
        return voteContentRepository.findByVoteTitle(voteTitle);
    }
    
    public Map<String, Long> getVoteResult(VoteTitle voteTitle) {
        List<VoteContent> contents = voteContentRepository.findByVoteTitle(voteTitle);
        Map<String, Long> result = new LinkedHashMap<>();

        for (VoteContent content : contents) {
            Long count = userVoteRepository.countByVoteContent(content);
            result.put(content.getContent(), count);
        }

        return result;
    }

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
}
