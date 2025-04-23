package com.ssgpack.ssgfc.user;

import java.security.Principal;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    @Autowired
    private UserService service;
    

    //  루트 URL 접근 시 로그인 페이지로 이동
    @RequestMapping("/")
    public String main() {
        return "user/login";
    }

    //  마이페이지 출력 (닉네임, 이메일, 권한 이름 포함)
    @GetMapping("/user/mypage")
    public String user(Model model, Principal principal) {
        User user = service.findByEmail(principal.getName());

        if (user == null) {
            throw new UsernameNotFoundException("유저 없음");
        }

        // 권한 코드 → 텍스트 이름 변환
        String roleName = "";
        switch (user.getRole()) {
            case 0: roleName = "마스터 관리자"; break;
            case 1: roleName = "유저 관리자"; break;
            case 2: roleName = "선수 관리자"; break;
            case 3: roleName = "게시판 관리자"; break;
            case 4: roleName = "경기일정 관리자"; break;
            case 5: roleName = "일반 사용자"; break;
            default: roleName = "알 수 없음";
        }

        model.addAttribute("user", user);
        model.addAttribute("roleName", roleName);
        return "user/mypage";
    }

    //  로그인 페이지 출력
    @GetMapping("/user/login")
    public String login() {
        return "user/login";
    }

    //  회원가입 페이지 출력
    @GetMapping("/user/join")
    public String join(UserForm userForm) {
        return "user/join";
    }

    //  회원가입 처리
    @PostMapping("/user/join")
    public String join(@Valid UserForm userForm, BindingResult bindingResult) {
        // 유효성 검사 실패 시 다시 회원가입 폼으로 이동
        if (bindingResult.hasErrors()) {
            return "user/join";
        }

        // 비밀번호 일치 확인
        if (!userForm.getPwd().equals(userForm.getPwd2())) {
            bindingResult.rejectValue("pwd2", "passwordInCorrect", "패스워드를 확인해주세요");
            return "user/join";
        }

        try {
            // 회원 정보 생성 및 저장
            User user = new User();
            user.setEmail(userForm.getEmail());
            user.setNick_name(userForm.getNick_name());
            user.setPwd(userForm.getPwd());
            user.setRole(1); // 기본 권한: 일반 사용자 (MEMBER)
            user.setIp();    // 가입 IP 저장
            service.insertMember(user);
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace();
            bindingResult.reject("failed", "이미 등록된 유저입니다.");
            return "user/join";
        } catch (Exception e) {
            e.printStackTrace();
            bindingResult.reject("failed", e.getMessage());
            return "user/join";
        }

        // 성공 시 로그인 페이지로 이동
        return "redirect:/user/login";
    }

    //  로그인 성공 시 메인 페이지로 이동
    @GetMapping("/main")
    public String mainPage() {
        return "main"; // templates/main.html
    }

    //  로그아웃 처리 (세션 무효화 및 시큐리티 초기화)
    @GetMapping("/user/logout")
    public String manualLogout(HttpServletRequest request, HttpServletResponse response) {
        // 기존 세션 종료
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Spring Security 컨텍스트 초기화
        SecurityContextHolder.clearContext();

        // 새로운 세션 생성 (인증된 세션 아님)
        request.getSession(true);

        // 로그인 페이지로 리다이렉트
        return "redirect:/user/login";
    }
    @GetMapping("/user/mypage/edit")
    public String editForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userDetails.getUser());
        return "user/edit"; // edit.html 렌더링
    }

    @PostMapping("/user/mypage/edit")
    public String editSubmit(@ModelAttribute("user") User updated,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpServletRequest request,
                             Model model) {
        try {
            service.updateUser(userDetails.getUser().getId(), updated);
        } catch (IllegalArgumentException e) {
            // 예외 메시지 전달
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", updated); // 사용자 입력값 유지
            return "user/edit";
        }

        // tp션 만료 + 시큐리티 컨텍스트 초기화 = 강제 로그아웃
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        // 로그인 페이지로 리다이렉트 + 안내용 파라미터
        return "redirect:/user/login?updated=true";
    }

 // 비밀번호 재발급 폼 출력
    @GetMapping("/user/password-reset")
    public String passwordResetForm() {
        return "user/password-reset"; // templates/user/password-reset.html
    }

    // 비밀번호 재발급 처리
    @PostMapping("/user/password-reset")
    public String passwordResetSubmit(@RequestParam("email") String email, Model model) {
        try {
            // 이메일 존재 여부 확인
            User user = service.findByEmail(email);

            // 임시 비밀번호 생성 (예: 10자리 알파벳+숫자) 이메일은 tempPassword로 보내야함 안그러면 원본이 아닌 암호화 된 비번 유저가 받음
            String tempPassword = UUID.randomUUID().toString().substring(0, 10);

            // 임시 비밀번호 암호화 후 저장
            user.setPwd(new BCryptPasswordEncoder().encode(tempPassword));
            service.update(user.getId(), user); // update 메서드는 기존에 구현돼 있어야 함

            // TODO: 추후 이메일로 tempPassword 전송
            model.addAttribute("message", "임시 비밀번호가 발급되었습니다. (추후 이메일 발송 예정)");

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "해당 이메일로 가입된 사용자가 없습니다.");
        }

        return "user/password-reset";
    }

}
