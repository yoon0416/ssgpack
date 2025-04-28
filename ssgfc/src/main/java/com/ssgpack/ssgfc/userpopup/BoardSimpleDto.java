package com.ssgpack.ssgfc.userpopup;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardSimpleDto {
    private Long id; // 게시글 ID
    private String title; // 게시글 제목
}
