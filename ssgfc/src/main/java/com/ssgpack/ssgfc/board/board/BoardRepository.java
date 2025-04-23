package com.ssgpack.ssgfc.board.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

	// ✅ 페이징 처리된 최신순 조회
	Page<Board> findAllByOrderByCreateDateDesc(Pageable pageable);

	// ✅ 전체 리스트 최신순 조회 (페이징 없이)
	List<Board> findAllByOrderByCreateDateDesc();

	// 키워드가 제목 또는 내용에 포함된 게시글 페이징 조회
	Page<Board> findByTitleContainingOrContentContainingOrderByCreateDateDesc(String title, String content,
			Pageable pageable);
}
