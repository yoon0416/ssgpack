package com.ssgpack.ssgfc.board.board;

import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardService boardService;

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
            String filename = file.getOriginalFilename();
            board.setImg(filename);
        }

        User user = userDetails.getUser(); // 🔐 로그인 유저 객체
        board.setIp();
        board.setUser(user);
        boardService.insert(board, user.getId());

        return "redirect:/board/list";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Board board = boardService.find(id);
        model.addAttribute("board", board);
        return "board/view";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Board board = boardService.find(id);
        model.addAttribute("board", board);
        return "board/edit";
    }

    @PostMapping("/edit")
    public String editSubmit(@Valid @ModelAttribute("board") Board board,
                             BindingResult result,
                             @RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (result.hasErrors()) {
            return "board/edit";
        }

        if (!file.isEmpty()) {
            String filename = file.getOriginalFilename();
            board.setImg(filename);
        }

        User user = userDetails.getUser(); // 🔐 로그인 유저 객체
        board.setIp();
        board.setUser(user);
        boardService.insert(board, user.getId());

        return "redirect:/board/list";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/board/list";
    }
}
