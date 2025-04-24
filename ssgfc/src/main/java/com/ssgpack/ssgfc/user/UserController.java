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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    @Autowired
    private UserService service;

    // 루트 URL 접근 시 로그인 페이지로 이동
    @RequestMapping("/")
    public String main() {
        return "user/login";
    }

    // 마이페이지 출력 (닉네임, 이메일, 권한 이름 포함)
    @GetMapping("/user/mypage")
    public String user(Model model, Principal principal) {
        User user = service.findByEmail(principal.getName());
        if (user == null) {
            throw new UsernameNotFoundException("유저 없음");
        }

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

    // 로그인 페이지 출력
    @GetMapping("/user/login")
    public String login() {
        return "user/login";
    }

    // 회원가입 폼 출력
    @GetMapping("/user/join")
    public String join(UserForm userForm) {
        return "user/join";
    }

    // 회원가입 처리
    @PostMapping("/user/join")
    public String join(@Valid UserForm userForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/join";
        }

        if (!userForm.getPwd().equals(userForm.getPwd2())) {
            bindingResult.rejectValue("pwd2", "passwordInCorrect", "패스워드를 확인해주세요");
            return "user/join";
        }

        try {
            User user = new User();
            user.setEmail(userForm.getEmail());
            user.setNick_name(userForm.getNick_name());
            user.setPwd(userForm.getPwd());
            user.setRole(1); // 기본 권한: 일반 사용자
            user.setIp();    // IP 저장
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

        return "redirect:/user/login";
    }

    // 로그인 성공 시 메인 페이지
    @GetMapping("/main")
    public String mainPage() {
        return "main";
    }

    // 수동 로그아웃 처리
    @GetMapping("/user/logout")
    public String manualLogout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        request.getSession(true);
        return "redirect:/user/login";
    }

    // 마이페이지 수정 폼
    @GetMapping("/user/mypage/edit")
    public String editForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userDetails.getUser());
        return "user/edit";
    }

    // 마이페이지 정보 수정 (이메일, 닉네임 수정만 가능)
    @PostMapping("/user/mypage/edit")
    public String editSubmit(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam String email,
                             @RequestParam String nick_name,
                             @RequestParam String currentPwd,
                             HttpServletRequest request,
                             Model model) {

        User user = userDetails.getUser();

        // 현재 비밀번호 확인
        if (!service.checkPassword(user, currentPwd)) {
            model.addAttribute("errorMessage", "현재 비밀번호가 올바르지 않습니다.");
            model.addAttribute("user", user);
            return "user/edit";
        }

        // 닉네임, 이메일만 수정 (비밀번호 변경 없음)
        user.setNick_name(nick_name);
        user.setEmail(email);
        service.updateUser(user.getId(), user); // updateUser에서는 비밀번호를 아예 변경하지 않음

        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/user/login?updated=true";
    }

    // 비밀번호 변경 폼 출력
    @GetMapping("/user/password/change")
    public String showChangePasswordForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userDetails.getUser());
        return "user/password-change";
    }

    // 비밀번호 변경 처리
    @PostMapping("/user/password/change")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String currentPwd,
                                 @RequestParam String newPwd,
                                 @RequestParam String newPwdCheck,
                                 HttpServletRequest request,
                                 Model model) {

        User user = userDetails.getUser();

        if (!service.checkPassword(user, currentPwd)) {
            model.addAttribute("errorMessage", "현재 비밀번호가 올바르지 않습니다.");
            model.addAttribute("user", user);
            return "user/password-change";
        }

        if (!newPwd.equals(newPwdCheck)) {
            model.addAttribute("errorMessage", "새 비밀번호가 일치하지 않습니다.");
            model.addAttribute("user", user);
            return "user/password-change";
        }

        user.setPwd(newPwd); // 평문으로 넘기고 service 내부에서 암호화
        service.update(user.getId(), user);

        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/user/login?pw_changed=true";
    }

    // 비밀번호 재발급 폼 출력
    @GetMapping("/user/password-reset")
    public String passwordResetForm() {
        return "user/password-reset";
    }

    // 비밀번호 재발급 처리
    @PostMapping("/user/password-reset")
    public String passwordResetSubmit(@RequestParam("email") String email, Model model) {
        try {
            User user = service.findByEmail(email);
            String tempPassword = UUID.randomUUID().toString().substring(0, 10);
            user.setPwd(tempPassword); // 평문으로 넘기고 service 내부에서 암호화
            service.update(user.getId(), user);
            model.addAttribute("message", "임시 비밀번호가 발급되었습니다. (추후 이메일 발송 예정)");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "해당 이메일로 가입된 사용자가 없습니다.");
        }
        return "user/password-reset";
    }

    // 회원 탈퇴 처리
    @PostMapping("/user/withdraw")
    public String withdraw(@AuthenticationPrincipal CustomUserDetails userDetails,
                           HttpServletRequest request) {
        User user = userDetails.getUser();

        // 유저 삭제 (연관 데이터도 ON DELETE CASCADE 적용)
        // 나중에는 진짜 삭제가 아니라 삭제한 척 하는 그 어 그 그그그그그 어 아무튼 그걸로 구현해봐야지
        service.delete(user.getId());

        // 세션 종료 및 시큐리티 초기화
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/user/login?withdrawn=true";
    }
}
