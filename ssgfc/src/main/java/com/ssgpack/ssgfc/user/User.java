//유저 엔티티

package com.ssgpack.ssgfc.user;

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
import javax.persistence.OneToMany;

import com.ssgpack.ssgfc.board.board.Board;
import com.ssgpack.ssgfc.board.comment.Comment;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity  
@Getter  
@Setter 
@ToString(exclude = "board")
public class User {
	//PK
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; 
	
	@Column(unique=true, nullable = false)  
	private String email; //이메일
	
	@Column(unique=true, nullable = false)   
	private String nick_name; //닉네임
	
	@Column(nullable = false)
	private String pwd; //비번
	
	@Column(updatable = false)
	private String ip; //가입할 때 ip
	
	@Column(updatable = false)
	private LocalDateTime createDate = LocalDateTime.now(); //가입날짜
	
	@OneToMany(mappedBy = "user")
	List<Board> board = new ArrayList<>();
	
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Comment> comments = new ArrayList<>();
	
	private int role;  //권한 ex)admin=0,1,2,3,4 멤버 : 5
	
	//ip생성하는 생성자
	public void setIp() {
		try {
			this.ip = InetAddress.getLocalHost().getHostAddress();
		} catch(Exception e) {
			e.printStackTrace();
			this.ip = null;
		}
	}
	
	/*
	@Column(nullable = false)
	private String phone_num; //전번
	@Column(columnDefinition = "TEXT")
	private String introduce; //소개글
	@Column(nullable = false)
	private int sex; //성별 남자0 여자1
	
	private String profile_img; //프사 이미지링크
	private int post_num; //우편번호
	private String adr; //주소
	private String adr_detail; //상세주소
	*/
	
}










