package com.ssgpack.ssgfc.report;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportListDto {

    // ✅ 신고 ID
    private Long reportId;

    // ✅ 신고 타입 (게시글 신고인지 댓글 신고인지)
    private ReportType reportType;

    // ✅ 신고 대상 작성자 닉네임
    private String nickname;

    // ✅ 게시글 제목 (게시글 신고일 경우만 값 존재)
    private String title;

    // ✅ 댓글 내용 (댓글 신고일 경우만 값 존재)
    private String content;

    // ✅ 신고 사유
    private String reason;

    // ✅ 신고 생성일 (yyyy-MM-dd HH:mm 포맷)
    private String createdDate;
    
    // ✅ 처리결과 
    private boolean processed;
    
    private Long boardId;
    
    private Long commentId;
    
    private Long parentCommentId;
    
    private Long targetId;
}
