package com.ssgpack.ssgfc.board.board;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // ✅ 공지 제외 일반 게시글 목록 (제목 + 닉네임 검색 포함)
    @Query(value = "SELECT new com.ssgpack.ssgfc.board.board.BoardListDto(" +
            "b.id, b.title, u.nick_name, u.id, FUNCTION('DATE_FORMAT', b.createDate, '%Y-%m-%d'), " +
            "b.hit, COUNT(DISTINCT l), COUNT(DISTINCT c)) " +
            "FROM Board b " +
            "LEFT JOIN b.user u " +
            "LEFT JOIN Like l ON l.board = b " +
            "LEFT JOIN Comment c ON c.board = b " +
            "WHERE (b.title IS NULL OR b.title NOT LIKE '[공지]%') " +
            "AND (:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.nick_name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "GROUP BY b.id, b.title, u.nick_name, u.id, b.createDate, b.hit " +
            "ORDER BY b.createDate DESC",
            countQuery = "SELECT COUNT(b) FROM Board b " +
                         "WHERE (b.title IS NULL OR b.title NOT LIKE '[공지]%') " +
                         "AND (:keyword IS NULL OR :keyword = '' OR " +
                         "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                         "LOWER(b.user.nick_name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BoardListDto> findAllBoardListWithCounts(@Param("keyword") String keyword, Pageable pageable);

    // ✅ 공지글만 조회할 때 사용
    List<Board> findByTitleStartingWithOrderByCreateDateDesc(String prefix);

    // ✅ 일반글 + 좋아요/댓글 수 포함된 DTO 조회
    @Query(value = "SELECT new com.ssgpack.ssgfc.board.board.BoardListDto(" +
            "b.id, b.title, u.nick_name, u.id, FUNCTION('DATE_FORMAT', b.createDate, '%Y-%m-%d'), " +
            "b.hit, COUNT(DISTINCT l), COUNT(DISTINCT c)) " +
            "FROM Board b " +
            "LEFT JOIN b.user u " +
            "LEFT JOIN Like l ON l.board = b " +
            "LEFT JOIN Comment c ON c.board = b " +
            "WHERE b.title IS NULL OR b.title NOT LIKE '[공지]%' " +
            "GROUP BY b.id, b.title, u.nick_name, u.id, b.createDate, b.hit " +
            "ORDER BY b.createDate DESC",
            countQuery = "SELECT COUNT(b) FROM Board b WHERE b.title IS NULL OR b.title NOT LIKE '[공지]%'")
    Page<BoardListDto> findAllBoardListWithCounts(Pageable pageable);
    
    // ✅ 인기글 조회할 때 사용
    @Query("SELECT new com.ssgpack.ssgfc.board.board.BoardListDto(" +
	       "b.id, b.title, u.nick_name, u.id, FUNCTION('DATE_FORMAT', b.createDate, '%Y-%m-%d'), " +
	       "b.hit, COUNT(DISTINCT l), COUNT(DISTINCT c), " +
	       "((b.hit / 10.0) + (COUNT(DISTINCT l) * 3) + " +
	       "(GREATEST(0, 48 - TIMESTAMPDIFF(HOUR, b.createDate, CURRENT_TIMESTAMP)) * 0.5))" +
	       ") " +
	       "FROM Board b " +
	       "LEFT JOIN b.user u " +
	       "LEFT JOIN Like l ON l.board = b " +
	       "LEFT JOIN Comment c ON c.board = b " +
	       "WHERE b.title IS NULL OR b.title NOT LIKE '[공지]%' " +
	       "GROUP BY b.id, b.title, u.nick_name, u.id, b.createDate, b.hit " +
	       "ORDER BY ((b.hit / 10.0) + (COUNT(DISTINCT l) * 3) + " +
	       "(GREATEST(0, 48 - TIMESTAMPDIFF(HOUR, b.createDate, CURRENT_TIMESTAMP)) * 0.5)) DESC")
	List<BoardListDto> findTopPopularBoards(Pageable pageable);

}
