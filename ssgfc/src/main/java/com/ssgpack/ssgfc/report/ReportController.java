package com.ssgpack.ssgfc.report;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.ssgpack.ssgfc.user.CustomUserDetails;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/report")
public class ReportController {

    private final ReportService reportService;

    // ✅ 게시글 신고
    @PostMapping("/board/{boardId}")
    public void reportBoard(@PathVariable Long boardId, @RequestBody ReportRequestDto requestDto, @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long reporterId = userDetails.getUser().getId(); // 🔥 reporter → reporterId로 수정
        reportService.saveBoardReport(
            boardId,         // 🔥 신고 대상 게시글 ID
            reporterId,      // 🔥 신고자 ID
            requestDto.getReason()
        );
    }

    // ✅ 댓글 신고
    @PostMapping("/comment/{commentId}")
    public void reportComment(@PathVariable Long commentId,
                              @RequestBody ReportRequestDto requestDto,
                              @AuthenticationPrincipal CustomUserDetails userDetails) {
    	
        Long reporterId = userDetails.getUser().getId(); // 🔥 reporter → reporterId로 수정
        
        reportService.saveCommentReport(
            requestDto.getBoardId(),     // 🔥 게시글 ID
            commentId,                   // 🔥 댓글 ID
            requestDto.getParentId(),    // 🔥 부모 댓글 ID
            reporterId,                  // 🔥 신고자 ID
            requestDto.getReason()
        );
    }

    // ✅ 미처리 신고 목록
    @GetMapping("/unprocessed")
    public List<ReportListDto> getUnprocessedReports(@RequestParam int page) {
        return reportService.findUnprocessedReports(page);
    }

    // ✅ 전체 신고 목록
    @GetMapping("/all")
    public List<ReportListDto> getAllReports(@RequestParam int page) {
        return reportService.findAllReports(page);
    }

    // ✅ 신고 처리
    @PostMapping("/process/{reportId}")
    public void processReport(@PathVariable Long reportId) {
        reportService.processReport(reportId);
    }
}