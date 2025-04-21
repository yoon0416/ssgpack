package com.ssgpack.ssgfc.board.comment;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardService;
import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board/{boardId}/comment")
public class CommentController {

    private final CommentService commentService;
    private final BoardService boardService;

    // ✅ 댓글 저장
    @PostMapping("/add")
    public String addComment(@PathVariable Long boardId,
                             @RequestParam String content,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpServletRequest request) {

        // 🔐 로그인 유저
        User user = userDetails.getUser();

        // 🧩 게시글 찾기
        Board board = boardService.findById(boardId);

        // 🌐 실제 IP 가져오기
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        // 💬 댓글 생성
        Comment comment = Comment.builder()
                .content(content)
                .user(user)
                .board(board)
                .ip(ip)
                .build();

        // 💾 저장
        commentService.save(comment);

        return "redirect:/board/view/" + boardId;
    }

    // ✅ 댓글 삭제
    @PostMapping("/{commentId}/delete")
    public String deleteComment(@PathVariable Long boardId,
                                @PathVariable Long commentId,
                                @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        commentService.deleteById(commentId, user);

        return "redirect:/board/view/" + boardId;
    }
}
