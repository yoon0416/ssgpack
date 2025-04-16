package com.ssgpack.ssgfc.board;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class BoardController {
	
	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}
}
