package com.ssgpack.ssgfc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Testcontroller {
	@GetMapping("/")
	public String test() {return "main";}
}
