//유저 엔티티

package com.ssgpack.ssgfc.user;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Entity  @Getter  @Setter
public class User {
	@Id		//기본키 설정
	@GeneratedValue(strategy = GenerationType.IDENTITY)//auto
	private Long id;       //<유저id>
	
	@Column(unique=true, nullable = false)  
	private String email; //이메일
	
	@Column(unique=true, nullable = false)   
	private String nick_name; //닉네임
	
	@Column(nullable = false)
	private String pwd; //비번
	
	private String ip; //가입할 때 ip
	
	
	@Column(updatable = false)  //한번 db에 저장되면 그 이후 update쿼리에서 제외
	private LocalDateTime udate = LocalDateTime.now(); //가입날짜
	
	
	
	/*
	@Column(nullable = false)
	private String phone_num; //전번
	@Column(columnDefinition = "TEXT")
	private String introduce; //소개글
	@Column(nullable = false)
	private int sex; //성별 남자0 여자1
	private int role;  //권한 ex)admin=0 일반유저=1
	private String profile_img; //프사 이미지링크
	private int post_num; //우편번호
	private String adr; //주소
	private String adr_detail; //상세주소
	*/
	
}










