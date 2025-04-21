package com.ssgpack.ssgfc.admin;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardService;
import com.ssgpack.ssgfc.board.comment.CommentService;
import com.ssgpack.ssgfc.user.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/board")
@RequiredArgsConstructor
public class AdminBoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    // 🔹 관리자용 게시판 목록 (기본 접근)
    @GetMapping
    public String adminBoardList(Model model) {
        model.addAttribute("boardList", boardService.findAll());
        return "admin/board/list"; // templates/admin/board/list.html
    }

    // 🔹 /admin/board/list 요청도 처리
    @GetMapping("/list")
    public String adminBoardListAlias(Model model) {
        model.addAttribute("boardList", boardService.findAll());
        return "admin/board/list";
    }

    // 🔹 관리자용 게시글 상세 보기
    @GetMapping("/view/{id}")
    public String adminBoardView(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "admin/board/view"; // templates/admin/board/view.html
    }

    // 🔹 관리자용 게시글 수정 폼
    @GetMapping("/edit/{id}")
    public String adminBoardEditForm(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "admin/board/edit"; // templates/admin/board/edit.html
    }

    // 🔹 관리자용 게시글 수정 처리
    @PostMapping("/edit/{id}")
    public String adminBoardEdit(@PathVariable Long id, @ModelAttribute Board board) {
        boardService.update(id, board);
        return "redirect:/admin/board/view/" + id;
    }

    // 🔹 관리자용 게시글 삭제
    @PostMapping("/delete/{id}")
    public String adminBoardDelete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/admin/board";
    }

    // 🔹 관리자용 댓글 삭제
    @PostMapping("/comment/delete/{commentId}")
    public String adminCommentDelete(@PathVariable Long commentId, @RequestParam Long boardId) {
        commentService.delete(commentId);
        return "redirect:/admin/board/view/" + boardId;
    }

    // 🔹 관리자용 글쓰기 폼 (공지사항 등록 포함)
    @GetMapping("/write")
    public String adminBoardWriteForm(Model model) {
        model.addAttribute("board", new Board());
        return "admin/board/write"; // templates/admin/board/write.html
    }

    // 🔹 관리자용 글쓰기 처리
    @PostMapping("/write")
    public String adminBoardWrite(@ModelAttribute Board board, 
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        board.setIp(); // IP 설정
        board.setUser(userDetails.getUser()); // 🔥 작성자 설정
        boardService.insert(board, userDetails.getUser().getId());
        return "redirect:/admin/board";
    }

}
