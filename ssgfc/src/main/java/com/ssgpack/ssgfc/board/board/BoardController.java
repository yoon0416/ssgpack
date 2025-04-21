package com.ssgpack.ssgfc.board.board;

import com.ssgpack.ssgfc.board.like.LikeService;
import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final LikeService likeService;

    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("boardList", boardService.findAll());
        return "board/list";
    }

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new Board());
        return "board/write";
    }

    @PostMapping("/write")
    public String writeSubmit(@Valid @ModelAttribute("board") Board board,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (result.hasErrors()) {
            return "board/write";
        }

        if (!file.isEmpty()) {
            board.setImg(file.getOriginalFilename());
        }

        User user = userDetails.getUser();
        board.setIp();
        board.setUser(user);
        boardService.insert(board, user.getId());

        return "redirect:/board/list";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model) {

        Board board = boardService.find(id);
        model.addAttribute("board", board);

        User user = userDetails != null ? userDetails.getUser() : null;
        boolean liked = (user != null) && likeService.isLikedByUser(board, user);
        long likeCount = likeService.countByBoard(board);

        model.addAttribute("liked", liked);
        model.addAttribute("likeCount", likeCount);

        if (user != null) {
            model.addAttribute("currentUserId", user.getId());
        }

        return "board/view";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        Board board = boardService.find(id);

        if (!board.getUser().getId().equals(userDetails.getUser().getId())) {
            return "redirect:/board/list";
        }

        model.addAttribute("board", board);
        return "board/edit";
    }

    @PostMapping("/edit")
    public String editSubmit(@Valid @ModelAttribute("board") Board board,
                             BindingResult result,
                             @RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Board original = boardService.find(board.getId());

        if (!original.getUser().getId().equals(userDetails.getUser().getId())) {
            return "redirect:/board/list";
        }

        if (result.hasErrors()) {
            return "board/edit";
        }

        if (!file.isEmpty()) {
            board.setImg(file.getOriginalFilename());
        }

        User user = userDetails.getUser();
        board.setIp();
        board.setUser(user);
        boardService.insert(board, user.getId());

        return "redirect:/board/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        Board board = boardService.find(id);

        if (!board.getUser().getId().equals(userDetails.getUser().getId())) {
            return "redirect:/board/list";
        }

        boardService.delete(id);
        return "redirect:/board/list";
    }
}
