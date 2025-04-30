package com.ssgpack.ssgfc.report;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.board.BoardRepository;
import com.ssgpack.ssgfc.board.comment.Comment;
import com.ssgpack.ssgfc.board.comment.CommentRepository;
import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    // ✅ 게시글 신고 저장
    public void saveBoardReport(Long boardId, Long reporterId, String reason) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

        Report report = Report.builder()
                .reportType(ReportType.BOARD)
                .targetId(boardId)
                .reporterId(reporterId)
                .targetWriterId(board.getUser().getId())
                .reason(reason)
                .boardId(board.getId())
                .build();

        reportRepository.save(report);

        List<Integer> adminRoles = List.of(0, 3);
        List<User> admins = userRepository.findByRoleIn(adminRoles);
        for (User admin : admins) {
            notificationService.saveNotification(admin, "새로운 신고가 접수되었습니다.", report.getId());
        }
    }

    // ✅ 댓글 신고 저장
    public void saveCommentReport(Long boardId, Long commentId, Long parentId, Long reporterId, String reason) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. boardId=" + boardId));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. commentId=" + commentId));

        Comment parent = null;
        if (parentId != null && parentId > 0) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다. parentId=" + parentId));
        }

        Report report = Report.builder()
                .reportType(ReportType.COMMENT)
                .targetId(commentId)
                .reporterId(reporterId)
                .targetWriterId(comment.getUser().getId())
                .reason(reason)
                .boardId(board.getId())
                .commentId(comment.getId())
                .parentCommentId(parent != null ? parent.getId() : null)
                .build();

        reportRepository.save(report);

        List<Integer> adminRoles = List.of(0, 3);
        List<User> admins = userRepository.findByRoleIn(adminRoles);
        for (User admin : admins) {
            notificationService.saveNotification(admin, "새로운 댓글 신고가 접수되었습니다.", report.getId());
        }
    }

    // ✅ 미처리 신고 목록
    public List<ReportListDto> findUnprocessedReports(int page) {
        return reportRepository.findUnprocessedReports(PageRequest.of(page, 10));
    }

    // ✅ 전체 신고 목록
    public List<ReportListDto> findAllReports(int page) {
        return reportRepository.findAllReports(PageRequest.of(page, 10));
    }

    // ✅ 신고 처리
    public void processReport(Long reportId) {
        reportRepository.markReportAsProcessed(reportId);
        notificationService.deleteByReportId(reportId);
    }

    // ✅ 내용삭제 처리
    public void deleteContentAndProcessReport(Long reportId, Long dummyUserId) {
        Report report = findById(reportId);
        User dummyUser = userRepository.findById(dummyUserId)
                .orElseThrow(() -> new IllegalArgumentException("더미 유저를 찾을 수 없습니다."));

        if (report.getReportType() == ReportType.BOARD) {
            Board board = boardRepository.findById(report.getBoardId())
                    .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
            board.setTitle("내용이 삭제되었습니다.");
            board.setContent("내용이 삭제되었습니다.");
            board.setUser(dummyUser);
            boardRepository.save(board);
        } else if (report.getReportType() == ReportType.COMMENT) {
            Comment comment = commentRepository.findById(report.getCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
            comment.setContent("내용이 삭제되었습니다.");
            comment.setUser(dummyUser);
            commentRepository.save(comment);
        }

        reportRepository.markReportAsProcessed(reportId);
        notificationService.deleteByReportId(reportId);
    }

    // ✅ 신고 번호 찾기
    public Report findById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("신고 정보를 찾을 수 없습니다."));
    }
}
