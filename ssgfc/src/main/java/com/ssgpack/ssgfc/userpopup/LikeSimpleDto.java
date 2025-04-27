package com.ssgpack.ssgfc.userpopup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LikeSimpleDto {
    private Long boardId; // 좋아요한 게시글 ID
    private String title; // 게시글 제목
}
