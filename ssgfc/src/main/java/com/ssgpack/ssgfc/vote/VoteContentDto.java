package com.ssgpack.ssgfc.vote;

import lombok.Builder;
import lombok.Data;

// ✅ 선택지(VoteContent) 정보를 화면에 표시할 때 사용할 DTO입니다. (ID, 내용, 투표 수 포함)
@Data
@Builder
public class VoteContentDto {
    private Long id;         // ✅ 선택지 ID
    private String content;  // ✅ 선택지 내용
    private long voteCount;  // ✅ 해당 선택지에 투표한 사람 수
}
