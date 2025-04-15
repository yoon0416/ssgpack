// 유저 컨트롤러 일단 테스트만 돌릴 예정
package com.ssgpack.ssgfc.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

public class UserController {

	@RequestMapping("/test1") //아직 안만들었음
	@ResponseBody // 해당정보만 출력
	public String basic1() { return "hello"; }
}
