package com.ssgpack.ssgfc.userpopup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentSimpleDto {
    private Long id; // 댓글 ID
    private String content; // 댓글 내용
}
