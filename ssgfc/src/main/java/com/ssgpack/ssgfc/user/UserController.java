package com.ssgpack.ssgfc.user;

import java.security.Principal;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {

    @Autowired
    private UserService service;

    // ✅ 루트 URL 요청 시 로그인 페이지로 이동
    @RequestMapping("/")
    public String main() {
        return "user/login";
    }

    // ✅ 로그인된 사용자의 마이페이지 출력 (닉네임, 이메일, 권한)
    @GetMapping("/user/mypage")
    public String user(Model model, Principal principal) {
        User user = service.findByEmail(principal.getName()); // 로그인한 사용자의 이메일로 조회
        model.addAttribute("user", user); // 조회된 user 객체를 model에 담아서 View로 전달
        return "user/mypage";
    }

    // ✅ 로그인 페이지 출력
    @GetMapping("/user/login")
    public String login() {
        return "user/login";
    }

    // ✅ 회원가입 페이지 출력
    @GetMapping("/user/join")
    public String join(UserForm memberForm) {
        return "user/join";
    }

    // ✅ 회원가입 처리 (POST)
    @PostMapping("/user/join")
    public String join(@Valid UserForm userForm, BindingResult bindingResult) {
        // 유효성 검사 실패 시 다시 회원가입 폼으로
        if (bindingResult.hasErrors()) {
            return "user/join";
        }

        // 비밀번호 확인이 일치하지 않을 경우 에러 반환
        if (!userForm.getPwd().equals(userForm.getPwd2())) {
            bindingResult.rejectValue("pwd2", "passwordInCorrect", "패스워드를 확인해주세요");
            return "user/join";
        }

        try {
            // User 엔티티 생성 및 저장
            User user = new User();
            user.setEmail(userForm.getEmail());
            user.setNick_name(userForm.getNick_name());
            user.setPwd(userForm.getPwd());
            user.setRole(1);     // 기본 권한: 일반 유저
            user.setIp();        // IP 자동 저장
            service.insertMember(user);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("failed", "등록된 유저입니다.");
            return "user/join";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("failed", e.getMessage());
            return "user/join";
        }

        // 회원가입 성공 시 로그인 페이지로 리다이렉트
        return "redirect:/user/login";
    }

    // ✅ 로그인 성공 후 이동할 메인 페이지
    @GetMapping("/main")
    public String mainPage() {
        return "main"; // templates/main.html
    }
}
