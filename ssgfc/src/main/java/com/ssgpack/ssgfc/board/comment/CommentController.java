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

    // ✅ 댓글 저장 (원댓글 & 대댓글 공통 처리)
    @PostMapping("/add")
    public String addComment(@PathVariable Long boardId,
                             @RequestParam String content,
                             @RequestParam(required = false) Long parentId,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpServletRequest request) {

        User user = userDetails.getUser();
        Board board = boardService.findById(boardId);

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }

        // ✅ 원댓글이든 대댓글이든 service에서 parentId 처리
        commentService.saveComment(boardId, user, content, ip, parentId);

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
