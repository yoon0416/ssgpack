package com.ssgpack.ssgfc.board.board;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;

import com.ssgpack.ssgfc.board.comment.Comment;
import com.ssgpack.ssgfc.board.like.Like;
import com.ssgpack.ssgfc.user.User;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "user")

public class Board {
	//PK
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; 
	
	@NotBlank(message = "제목은 필수입니다.")
	@Column(length=200, nullable = false)
	private String title;
	
	@NotBlank(message = "내용은 필수입니다.")
	@Column(columnDefinition = "text", nullable = false)
	private String content;
	
	@Column(updatable = false)
	private LocalDateTime createDate = LocalDateTime.now();
	
	@Column(updatable = false)
	private String ip; 
	
	private Long hit=0L;
	
	private String img;
	
	@ManyToOne   
	private  User user;

	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> comments = new ArrayList<>();
	
	//ip생성하는 생성자
	public void setIp() {
		try {
			this.ip = InetAddress.getLocalHost().getHostAddress();
		} catch(Exception e) {
			e.printStackTrace();
			this.ip = null;
		}
	}
	
	@OneToMany(mappedBy = "board", cascade = CascadeType.ALL)
	private List<Like> likes;
}
