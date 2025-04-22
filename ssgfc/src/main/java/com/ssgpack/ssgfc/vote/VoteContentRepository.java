package com.ssgpack.ssgfc.vote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoteContentRepository extends JpaRepository<VoteContent, Long> {
    List<VoteContent> findByVoteTitle(VoteTitle voteTitle);
}
