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
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    private final LikeService likeService;

    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword) {

        // 공지글: 모든 게시글 중에서 title이 [공지]로 시작하는 것만 필터링
        List<Board> allBoards = boardService.findAll();
        List<Board> noticeBoards = allBoards.stream()
            .filter(b -> b.getTitle() != null && b.getTitle().startsWith("[공지]"))
            .collect(Collectors.toList());

        // 일반 게시글: 공지를 제외한 나머지 + 검색 키워드 대응
        Page<Board> pagingList = boardService.getPagingExcludeNotice(page, keyword);
        PagingDto paging = new PagingDto((int) pagingList.getTotalElements(), page);

        model.addAttribute("noticeBoards", noticeBoards);
        model.addAttribute("normalBoards", pagingList.getContent());
        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);

        return "board/list";
    }

    @GetMapping("/popular")
    public String popular(Model model) {
        List<Board> popularBoards = boardService.getPopularBoards(3);
        model.addAttribute("popularBoards", popularBoards);
        return "redirect:/";
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
                              @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        if (result.hasErrors()) {
            return "board/write";
        }

        User user = userDetails.getUser();
        board.setUser(user);
        boardService.insert(board, user.getId(), file);

        return "redirect:/board/list";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       Model model,
                       HttpSession session) {

        Set<Long> viewedBoards = (Set<Long>) session.getAttribute("viewedBoards");
        if (viewedBoards == null) {
            viewedBoards = new HashSet<>();
        }

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
