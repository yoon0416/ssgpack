package com.ssgpack.ssgfc.board;

import java.net.InetAddress;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString//(exclude = "user")

public class Board {
	//PK
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; 
	
	@Column(length=200)
	private String title;
	
	@Column(columnDefinition = "text")
	private String content;
	
	@Column(updatable = false)
	private LocalDateTime createDate = LocalDateTime.now();
	
	@Column(updatable = false)
	private String ip; 
	
	private Long hit=0L;
	
	private String img;
	
	//@ManyToOne   
	//private  User user;

	//ip생성하는 생성자
	public void setIp() {
		try {
			this.ip = InetAddress.getLocalHost().getHostAddress();
		} catch(Exception e) {
			e.printStackTrace();
			this.ip = null;
		}
	}
	
}
