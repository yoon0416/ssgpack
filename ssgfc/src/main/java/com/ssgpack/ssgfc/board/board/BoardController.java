package com.ssgpack.ssgfc.board.board;

import com.ssgpack.ssgfc.board.like.LikeService;
import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    public String list(@PageableDefault(size = 10, sort = "createDate", direction = Sort.Direction.DESC) Pageable pageable,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        // 공지글 별도 조회
        List<Board> noticeBoards = boardService.findNoticeBoards();

        // 일반글 + 검색 포함 DTO 조회
        Page<BoardListDto> normalBoards = boardService.getBoardListWithCounts(keyword, pageable);
        PagingDto paging = boardService.createPagingDto(normalBoards);

        model.addAttribute("noticeBoards", noticeBoards);
        model.addAttribute("normalBoards", normalBoards.getContent());
        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);
        return "board/list";
    }

    // ✅ 인기글 조회 (메인페이지 등에서 사용)
    @GetMapping("/popular")
    public String popular(Model model) {
        List<BoardListDto> popularBoards = boardService.getPopularBoards(3);
        model.addAttribute("popularBoards", popularBoards);
        return "redirect:/"; // ✅ main.html에서 바로 popularBoards 출력에 활용
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

    // ✅ 게시글 상세조회 (조회수 증가 + 좋아요 포함)
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model,
                       HttpSession session) {

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
