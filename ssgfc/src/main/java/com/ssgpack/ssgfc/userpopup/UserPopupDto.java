package com.ssgpack.ssgfc.userpopup;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class UserPopupDto {
    private String nickName; // 닉네임
    private String profileImg; // 프로필 이미지 경로
    private String introduce; // 소개글
    private LocalDateTime createDate; // 가입일

    private List<BoardSimpleDto> boards; // 작성한 글
    private List<CommentSimpleDto> comments; // 작성한 댓글
    private List<LikeSimpleDto> likes; // 좋아요한 글
}
