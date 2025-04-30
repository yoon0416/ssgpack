package com.ssgpack.ssgfc.board.board;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardListDto {
    private Long id;
    private String title;
    private String nick_name;
    private Long userId;
    private String createDate;
    private Long hit;
    private Long likeCount;
    private Long commentCount;
    private double score;
    
    public BoardListDto(Long id, String title, String nick_name, Long userId, String createDate, Long hit, Long likeCount, Long commentCount) {
        this.id = id;
        this.title = title;
        this.nick_name = nick_name;
        this.userId = userId;
        this.createDate = createDate;
        this.hit = hit;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }
    
    public BoardListDto(Long id, String title, String nick_name, Long userId, String createDate,
            Long hit, Long likeCount, Long commentCount, double score) {
		this.id = id;
		this.title = title;
		this.nick_name = nick_name;
		this.userId = userId;
		this.createDate = createDate;
		this.hit = hit;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
		this.score = score;
	}
}
