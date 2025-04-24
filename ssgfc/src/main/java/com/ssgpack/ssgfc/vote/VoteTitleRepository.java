package com.ssgpack.ssgfc.vote;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteTitleRepository extends JpaRepository<VoteTitle, Long> {
	
	// ✅ 최신 투표 1건을 createDate 기준으로 정렬해 가져오는 메서드입니다.
	VoteTitle findTopByOrderByCreateDateDesc();

	// ✅ 전체 투표를 최신순으로 정렬해 반환
	List<VoteTitle> findAllByOrderByCreateDateDesc();
}
