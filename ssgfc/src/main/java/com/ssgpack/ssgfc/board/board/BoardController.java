package com.ssgpack.ssgfc.board.board;

import com.ssgpack.ssgfc.board.like.LikeService;
import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final LikeService likeService;

    // ✅ 게시글 목록 (공지 + 일반 + 검색 + 페이징)
    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword) {

        // ✅ 공지글만 따로 조회
        List<Board> noticeBoards = boardService.findNoticeBoards();

        // ✅ 일반 게시글 페이징 + 검색 포함
        Page<Board> pagingList = boardService.getPaging(page, keyword);
        PagingDto paging = new PagingDto((int) pagingList.getTotalElements(), page);

        model.addAttribute("noticeBoards", noticeBoards);
        model.addAttribute("normalBoards", pagingList.getContent());
        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);

        return "board/list";
    }

    // ✅ 인기글 조회
    @GetMapping("/popular")
    public String popular(Model model) {
        List<Board> popularBoards = boardService.getPopularBoards(3);
        model.addAttribute("popularBoards", popularBoards);
        return "redirect:/";
    }

    // ✅ 글쓰기 폼
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new Board());
        return "board/write";
    }

    // ✅ 글쓰기 처리
    @PostMapping("/write")
    public String writeSubmit(@Valid @ModelAttribute("board") Board board,
                              BindingResult result,
                              @RequestParam("file") MultipartFile file,
                              @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        if (result.hasErrors()) return "board/write";

        User user = userDetails.getUser();
        board.setUser(user);
        boardService.insert(board, user.getId(), file);

        return "redirect:/board/list";
    }

    // ✅ 게시글 상세조회 (조회수 증가, 좋아요 여부 포함)
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model,
                       HttpSession session) {

        // ✅ 조회수 중복 증가 방지
        Set<Long> viewedBoards = (Set<Long>) session.getAttribute("viewedBoards");
        if (viewedBoards == null) viewedBoards = new HashSet<>();

        if (!viewedBoards.contains(id)) {
            boardService.increaseViewCount(id);
            viewedBoards.add(id);
            session.setAttribute("viewedBoards", viewedBoards);
        }

        Board board = boardService.find(id);
        model.addAttribute("board", board);

        User user = userDetails != null ? userDetails.getUser() : null;
        boolean liked = (user != null) && likeService.isLikedByUser(board, user);
        long likeCount = likeService.countByBoard(board);

        model.addAttribute("liked", liked);
        model.addAttribute("likeCount", likeCount);
        if (user != null) model.addAttribute("currentUserId", user.getId());

        return "board/view";
    }

    // ✅ 게시글 수정 폼
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

    // ✅ 게시글 수정 처리
    @PostMapping("/edit")
    public String editSubmit(@Valid @ModelAttribute("board") Board board,
                             BindingResult result,
                             @RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        Board original = boardService.find(board.getId());

        if (!original.getUser().getId().equals(userDetails.getUser().getId())) {
            return "redirect:/board/list";
        }

        if (result.hasErrors()) {
            return "board/edit";
        }

        board.setUser(userDetails.getUser());
        board.setIp();
        boardService.update(board.getId(), board, file);

        return "redirect:/board/view/" + board.getId();
    }

    // ✅ 게시글 삭제
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
