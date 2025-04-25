package com.ssgpack.ssgfc.board.comment;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardRepository;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    // ✅ 게시글 ID를 기준으로 댓글 전체를 조회합니다 (원댓글 + 대댓글 포함)
    public List<Comment> getCommentsByBoard(Long boardId) {
        return commentRepository.findByBoardId(boardId);
    }

    // ✅ 댓글 저장 (원댓글 또는 대댓글 처리 가능)
    @Transactional
    public void saveComment(Long boardId, User user, String content, String ip, Long parentId) {
        // 📌 게시글 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 📌 대댓글인 경우 부모 댓글 조회
        Comment parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
        }

        // 📌 댓글 엔티티 생성
        Comment comment = Comment.builder()
                .board(board)
                .user(user)
                .content(content)
                .ip(ip)
                .parent(parent) // ✅ 대댓글이면 부모 댓글 설정
                .build();

        commentRepository.save(comment);
    }

    // ✅ 댓글 삭제 (일반 사용자용 - 본인 댓글만 삭제 가능)
    @Transactional
    public void deleteById(Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 🔐 본인 확인
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }

    // ✅ 댓글 삭제 (관리자 전용 - 작성자 확인 없이 삭제)
    @Transactional
    public void delete(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
