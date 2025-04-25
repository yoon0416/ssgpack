package com.ssgpack.ssgfc.admin;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardService;
import com.ssgpack.ssgfc.board.board.PagingDto;
import com.ssgpack.ssgfc.board.comment.CommentService;
import com.ssgpack.ssgfc.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.io.IOException;
import java.util.*;


//✅ 관리자 전용 게시판 컨트롤러 - 공지 + 일반글 모두 처리 - CRUD + 댓글 관리 포함
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/board")
public class AdminBoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    
    //✅ 관리자 게시판 목록 페이지 - 공지글 + 일반글 통합 후 최신순 정렬
    @GetMapping({"/", "/list"})
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        // ✅ 공지글만 별도 조회 (최신순)
        List<Board> noticeBoards = boardService.findNoticeBoards();

        // ✅ 일반 게시글만 페이징 + 검색
        Page<Board> normalPaging = boardService.getPaging(page, keyword);
        PagingDto paging = new PagingDto((int) normalPaging.getTotalElements(), page);

        // ✅ 공지 + 일반 합치기 (공지 먼저)
        List<Board> mergedList = new ArrayList<>();
        mergedList.addAll(noticeBoards);
        mergedList.addAll(normalPaging.getContent());

        // ✅ 모델로 전달
        model.addAttribute("boardList", mergedList);
        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);

        return "admin/board/list";
    }


    //✅ 게시글 상세 보기
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "admin/board/view";
    }

    //✅ 공지글 작성 폼
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new Board());
        return "admin/board/write";
    }

    
    //✅ 공지글 작성 처리 - 제목 앞에 [공지] 자동 추가
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

    //✅ 공지글 수정 폼
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Board board = boardService.findById(id);
        model.addAttribute("board", board);
        return "admin/board/edit";
    }

    //✅ 공지글 수정 처리
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id,
                       @ModelAttribute Board board,
                       @RequestParam("file") MultipartFile file) throws IOException {
        board.setIp();
        boardService.update(id, board, file);
        return "redirect:/admin/board/view/" + id;
    }

    //✅ 게시글 삭제 (강제 삭제)
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/admin/board/list";
    }

    //✅ 댓글 삭제 (관리자용)
    @PostMapping("/comment/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam("boardId") Long boardId) {
        commentService.delete(commentId);
        return "redirect:/admin/board/view/" + boardId;
    }
}
