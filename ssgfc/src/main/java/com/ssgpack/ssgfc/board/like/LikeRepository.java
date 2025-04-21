package com.ssgpack.ssgfc.board.like;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, LikeId> {
    
    Optional<Like> findByBoardAndUser(Board board, User user);

    int countByBoard(Board board);

    void deleteByBoardAndUser(Board board, User user);

    boolean existsByBoardAndUser(Board board, User user);
}
