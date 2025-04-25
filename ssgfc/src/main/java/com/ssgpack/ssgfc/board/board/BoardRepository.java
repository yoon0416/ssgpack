package com.ssgpack.ssgfc.board.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // ✅ 전체 게시글 최신순 (공지 포함)
    List<Board> findAllByOrderByCreateDateDesc();

    // ✅ [공지]로 시작하는 게시글 최신순 정렬
    List<Board> findByTitleStartingWithOrderByCreateDateDesc(String prefix);

    // ✅ 공지 제외 일반 게시글 페이징 (검색어 없음)
    @Query("SELECT b FROM Board b WHERE b.title IS NULL OR b.title NOT LIKE '[공지]%' ORDER BY b.createDate DESC")
    Page<Board> findExcludeNotice(Pageable pageable);

    // ✅ 공지 제외 일반 게시글 페이징 (검색어 있음)
    @Query("SELECT b FROM Board b WHERE (b.title IS NULL OR b.title NOT LIKE '[공지]%') AND " +
           "(b.title LIKE %:keyword% OR b.content LIKE %:keyword%) ORDER BY b.createDate DESC")
    Page<Board> findExcludeNoticeByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
