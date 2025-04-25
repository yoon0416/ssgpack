package com.ssgpack.ssgfc.admin;

import java.io.IOException;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import com.ssgpack.ssgfc.board.board.PagingDto;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardService;
import com.ssgpack.ssgfc.board.comment.CommentService;
import com.ssgpack.ssgfc.user.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/board")
public class AdminBoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    // 게시글 목록 (공지 + 일반)
    @GetMapping({"/", "/list"})
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        Page<Board> pagingList = boardService.getPaging(page, keyword);
        PagingDto paging = new PagingDto((int) pagingList.getTotalElements(), page);

        model.addAttribute("boardList", pagingList.getContent());
        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);

        return "admin/board/list";
    }


    // 게시글 상세 보기
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "admin/board/view";
    }

    // 글쓰기 폼 (공지사항 전용)
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new Board());
        return "admin/board/write";
    }

    // 글쓰기 처리
    @PostMapping("/write")
    public String write(@ModelAttribute Board board,
                        @RequestParam("file") MultipartFile file,
                        @AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
        board.setIp();
        board.setUser(userDetails.getUser());

        if (!board.getTitle().startsWith("[공지]")) {
            board.setTitle("[공지] " + board.getTitle());
        }

        boardService.insert(board, userDetails.getUser().getId(), file);
        return "redirect:/admin/board/list";
    }


    // 수정 폼
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "admin/board/edit";
    }

    // 수정 처리
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute Board board,
                       @RequestParam("file") MultipartFile file) throws IOException {
        board.setIp();
        boardService.update(id, board, file);
        return "redirect:/admin/board/view/" + id;
    }

    // 강제 삭제
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/admin/board/list";
    }

    // 댓글 삭제
    @PostMapping("/comment/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam("boardId") Long boardId) {
        commentService.delete(commentId);
        return "redirect:/admin/board/view/" + boardId;
    }
}
