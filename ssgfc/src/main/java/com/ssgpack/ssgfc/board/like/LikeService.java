package com.ssgpack.ssgfc.board.like;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    @Transactional
    public void toggleLike(User user, Board board) {
        Like like = likeRepository.findByBoardAndUser(board, user).orElse(null);

        if (like != null) {
            likeRepository.delete(like); // 좋아요 취소
        } else {
            Like newLike = Like.builder()
                    .user(user)
                    .board(board)
                    .build();
            likeRepository.save(newLike); // 좋아요 추가
        }
    }

    public boolean isLikedByUser(Board board, User user) {
        return likeRepository.existsByBoardAndUser(board, user);
    }

    public long countByBoard(Board board) {
        return likeRepository.countByBoard(board);
    }
}
