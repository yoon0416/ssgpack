package com.ssgpack.ssgfc.board.like;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByBoardIdAndUserId(Long boardId, Long userId);
    void deleteByBoardIdAndUserId(Long boardId, Long userId);
    Long countByBoardId(Long boardId);
}
