package com.ssgpack.ssgfc.report;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {

    // ✅ 미처리만 조회
    @Query("SELECT new com.ssgpack.ssgfc.report.ReportListDto(r.id, r.reportType, u.nick_name, " +
            "CASE WHEN r.reportType = 'BOARD' THEN b.title ELSE null END, " +
            "CASE WHEN r.reportType = 'COMMENT' THEN c.content ELSE null END, " +
            "r.reason, FUNCTION('DATE_FORMAT', r.createdDate, '%Y-%m-%d %H:%i'), r.processed, " +
            "r.boardId, r.commentId, r.parentCommentId, r.targetId) " +
            "FROM Report r " +
            "LEFT JOIN User u ON r.reporterId = u.id " +
            "LEFT JOIN Board b ON r.reportType = 'BOARD' AND r.targetId = b.id " +
            "LEFT JOIN Comment c ON r.reportType = 'COMMENT' AND r.targetId = c.id " +
            "WHERE r.processed = false " +
            "ORDER BY r.createdDate DESC")
    List<ReportListDto> findUnprocessedReports(Pageable pageable);

    // ✅ 전체 조회
    @Query("SELECT new com.ssgpack.ssgfc.report.ReportListDto(r.id, r.reportType, u.nick_name, " +
            "CASE WHEN r.reportType = 'BOARD' THEN b.title ELSE null END, " +
            "CASE WHEN r.reportType = 'COMMENT' THEN c.content ELSE null END, " +
            "r.reason, FUNCTION('DATE_FORMAT', r.createdDate, '%Y-%m-%d %H:%i'), r.processed, " +
            "r.boardId, r.commentId, r.parentCommentId, r.targetId) " +
            "FROM Report r " +
            "LEFT JOIN User u ON r.reporterId = u.id " +
            "LEFT JOIN Board b ON r.reportType = 'BOARD' AND r.targetId = b.id " +
            "LEFT JOIN Comment c ON r.reportType = 'COMMENT' AND r.targetId = c.id " +
            "ORDER BY r.createdDate DESC")
    List<ReportListDto> findAllReports(Pageable pageable);

    // ✅ 신고 처리 완료 업데이트
    @Modifying
    @Query("UPDATE Report r SET r.processed = true WHERE r.id = :id")
    void markReportAsProcessed(Long id);
}
