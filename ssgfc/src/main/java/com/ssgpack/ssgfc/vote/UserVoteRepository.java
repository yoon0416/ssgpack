package com.ssgpack.ssgfc.vote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVoteRepository extends JpaRepository<UserVote, UserVoteId> {
    Optional<UserVote> findByUserIdAndVoteTitleId(Long userId, Long voteTitleId);
    long countByVoteContent(VoteContent content);
}
