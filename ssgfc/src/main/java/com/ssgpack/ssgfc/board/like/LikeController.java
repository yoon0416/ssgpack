package com.ssgpack.ssgfc.board.like;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardService;
import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class LikeController {

    private final LikeService likeService;
    private final BoardService boardService;

    @PostMapping("/{id}/like")
    public String toggleLike(@PathVariable Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {

        // ✅ 로그인 안 했을 경우 → 로그인 페이지로 리디렉션
        if (userDetails == null) {
            return "redirect:/user/login"; // 로그인 페이지 URL
        }

        User user = userDetails.getUser();
        Board board = boardService.findById(id);

        if (board == null) {
            return "redirect:/board/list";
        }

        likeService.toggleLike(user, board);
        return "redirect:/board/view/" + id;
    }
}
