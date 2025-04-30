package com.ssgpack.ssgfc.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthNoticeController {

    @GetMapping("/auth-required")
    public String showAuthRequiredPage() {
        return "user/auth-required";
    }
}
