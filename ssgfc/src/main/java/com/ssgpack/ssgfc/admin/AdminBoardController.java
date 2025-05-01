package com.ssgpack.ssgfc.admin;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardListDto;
import com.ssgpack.ssgfc.board.board.BoardService;
import com.ssgpack.ssgfc.board.board.PagingDto;
import com.ssgpack.ssgfc.board.comment.Comment;
import com.ssgpack.ssgfc.board.comment.CommentService;
import com.ssgpack.ssgfc.report.Report;
import com.ssgpack.ssgfc.report.ReportService;
import com.ssgpack.ssgfc.user.CustomUserDetails;

import lombok.RequiredArgsConstructor;


//✅ 관리자 전용 게시판 컨트롤러 - 공지 + 일반글 모두 처리 - CRUD + 댓글 관리 포함
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/board")
public class AdminBoardController {

    private final BoardService boardService;
    private final CommentService commentService;
    private final ReportService reportService;

    
    //✅ 관리자 게시판 목록 페이지 - 공지글 + 일반글 통합 후 최신순 정렬
    @GetMapping({"/", "/list"})
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "keyword", required = false) String keyword,
                       Model model) {

        // ✅ 공지글만 따로 조회
        List<Board> noticeBoards = boardService.findNoticeBoards();

        // ✅ Pageable 생성
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "createDate"));

        // ✅ DTO 기반 일반글 조회 (검색 포함)
        Page<BoardListDto> boardPage = boardService.getBoardListWithCounts(keyword, pageable);
        PagingDto paging = boardService.createPagingDto(boardPage);

        // ✅ 모델에 전달
        model.addAttribute("noticeBoards", noticeBoards); // 공지
        model.addAttribute("normalBoards", boardPage.getContent()); // 일반글 DTO
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
    
  //✅ 리포트 게시글 상세 보기
    @GetMapping("/report/view/{boardId}")
    public String viewReport(
        @PathVariable Long boardId,
        @RequestParam(required = false) Long reportId,
        @RequestParam(required = false) Long commentId,
        @RequestParam(required = false) Long parentId,
        Model model) {

        // ✅ 게시글 존재 여부 확인
        Board board = boardService.findByIdOrNull(boardId);
        if (board == null) {
            if (reportId != null) {
                reportService.processReport(reportId); // 신고는 처리 상태로 전환
            }
            model.addAttribute("errorMessage", "⚠️ 해당 게시글은 삭제된 상태입니다.");
            return "admin/board/alert"; // alert 템플릿 사용
        }

        // ✅ 댓글 존재 여부 확인
        if (commentId != null) {
            Comment comment = commentService.findByIdOrNull(commentId); // 댓글이 존재하지 않을 수 있음
            if (comment == null) {
                if (reportId != null) {
                    reportService.processReport(reportId);
                }
                model.addAttribute("errorMessage", "⚠️ 해당 댓글은 삭제된 상태입니다.");
                return "admin/board/alert";
            }
        }

        model.addAttribute("board", board);

        if (reportId != null) {
            Report report = reportService.findById(reportId);
            model.addAttribute("report", report);
            model.addAttribute("reportType", report.getReportType());
        }

        model.addAttribute("commentId", commentId);
        model.addAttribute("parentId", parentId);

        return "admin/board/view";
    }

    
    //✅ 유지하기처기 버튼
    @PostMapping("/report/approve/{reportId}")
    @ResponseBody
    public void approveReport(@PathVariable Long reportId) {
        reportService.processReport(reportId); // 신고 처리 완료 + 알림 삭제
    }
    
    //✅ 내용삭제처리 버튼
    @PostMapping("/report/delete-content/{reportId}")
    @ResponseBody
    public void deleteContentFromReport(@PathVariable Long reportId) {
        reportService.deleteContentAndProcessReport(reportId, 2L); // 2번 더미 유저로 변경 + 처리 완료
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
    
    //✅ 신고 관리 페이지용 글 삭제 (리포트 전용)
    @PostMapping("/report/delete/{id}")
    public String deleteFromReport(@PathVariable Long id) {
        boardService.delete(id);
        return "redirect:/admin/report/list"; // 신고 관리 페이지로 리다이렉트
    }

    //✅ 댓글 삭제 (관리자용)
    @PostMapping("/comment/delete/{commentId}")
    public String deleteComment(@PathVariable Long commentId,
                                @RequestParam("boardId") Long boardId) {
        commentService.delete(commentId);
        return "redirect:/admin/board/view/" + boardId;
    }

}
