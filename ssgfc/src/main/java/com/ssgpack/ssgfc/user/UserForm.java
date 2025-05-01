package com.ssgpack.ssgfc.user;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter 
public class UserForm {
	
	@NotEmpty(message="닉네임은 필수 항목입니다.")
	@Size(min=2,max=20)
	private String nick_name;
	
	@NotEmpty(message="비밀번호는 필수 항목입니다.")
	private String pwd;
	
	@NotEmpty(message="비밀번호 확인은 필수 항목입니다")
	private String pwd2;
	
	@Email(message = "이메일 형식이 올바르지 않습니다.")
	@NotEmpty(message = "이메일은 필수 항목입니다.")
	private String email;
	
	
}