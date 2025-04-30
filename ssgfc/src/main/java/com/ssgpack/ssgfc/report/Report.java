package com.ssgpack.ssgfc.report;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Report {

    // ✅ PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 신고 종류 (게시글 신고인지 댓글 신고인지 구분)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;
    
    // ✅ 신고 대상 작성자 ID
    @Column(nullable = false)
    private Long targetWriterId;
    
    // ✅ 신고 대상 ID (게시글 ID 또는 댓글 ID)
    @Column(nullable = false)
    private Long targetId;

    // ✅ 신고자 ID (User ID 저장)
    @Column(nullable = false)
    private Long reporterId;

    // ✅ 신고 사유
    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    // ✅ 신고 처리 여부 (기본 false)
    @Column(nullable = false)
    private boolean processed = false;

    // ✅ 신고 작성일
    @Column(updatable = false)
    private LocalDateTime createdDate;

    // ✅ 저장 직전 작성일 자동 세팅
    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
    }

    // ✅ 추가: 신고된 댓글이 속한 게시글 ID
    private Long boardId;

    // ✅ 추가: 신고된 댓글 ID
    private Long commentId;

    // ✅ 추가: 신고된 댓글의 부모 댓글 ID (있을 경우만)
    private Long parentCommentId;
}
