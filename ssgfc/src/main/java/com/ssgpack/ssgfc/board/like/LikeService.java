package com.ssgpack.ssgfc.board.like;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardRepository;
import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LikeService {

    private final LikeRepository likeRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public boolean toggleLike(Long boardId, Long userId) {
        if (likeRepository.existsByBoardIdAndUserId(boardId, userId)) {
            likeRepository.deleteByBoardIdAndUserId(boardId, userId);
            return false;
        } else {
            Board board = boardRepository.findById(boardId).orElseThrow();
            User user = userRepository.findById(userId).orElseThrow();
            likeRepository.save(Like.builder().board(board).user(user).build());
            return true;
        }
    }

    public long countLikes(Long boardId) {
        return likeRepository.countByBoardId(boardId);
    }
}
