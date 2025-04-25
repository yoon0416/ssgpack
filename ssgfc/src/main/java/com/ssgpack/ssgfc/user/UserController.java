package com.ssgpack.ssgfc.user;

import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private EmailService emailService;

    @RequestMapping("/")
    public String main() {
        return "user/login";
    }

    @GetMapping("/user/mypage")
    public String user(Model model, Principal principal) {
        User user = service.findByEmail(principal.getName());
        if (user == null) throw new UsernameNotFoundException("유저 없음");

        String roleName = "";
        int role = user.getRole();
        if (role == 0) roleName = "마스터 관리자";
        else if (role == 1) roleName = "유저 관리자";
        else if (role == 2) roleName = "선수 관리자";
        else if (role == 3) roleName = "게시판 관리자";
        else if (role == 4) roleName = "경기일정 관리자";
        else if (role == 5) roleName = "일반 사용자";
        else roleName = "알 수 없음";

        model.addAttribute("user", user);
        model.addAttribute("roleName", roleName);
        return "user/mypage";
    }

    @GetMapping("/user/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/user/join")
    public String join(UserForm userForm) {
        return "user/join";
    }

    @PostMapping("/user/join")
    public String join(@Valid UserForm userForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) return "user/join";

        if (!userForm.getPwd().equals(userForm.getPwd2())) {
            bindingResult.rejectValue("pwd2", "passwordInCorrect", "패스워드를 확인해주세요");
            return "user/join";
        }

        try {
            User user = new User();
            user.setEmail(userForm.getEmail());
            user.setNick_name(userForm.getNick_name());
            user.setPwd(userForm.getPwd());
            user.setRole(1);
            user.setIp();
            service.insertMember(user);
        } catch (DataIntegrityViolationException e) {
            bindingResult.reject("failed", "이미 등록된 유저입니다.");
            return "user/join";
        } catch (Exception e) {
            bindingResult.reject("failed", e.getMessage());
            return "user/join";
        }

        return "redirect:/user/login";
    }

    @GetMapping("/main")
    public String mainPage() {
        return "main";
    }

    @GetMapping("/user/logout")
    public String manualLogout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        request.getSession(true);
        return "redirect:/user/login";
    }

    @GetMapping("/user/mypage/edit")
    public String editForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userDetails.getUser());
        return "user/edit";
    }

    @PostMapping("/user/mypage/edit")
    public String editSubmit(@AuthenticationPrincipal CustomUserDetails userDetails,
                             @RequestParam String email,
                             @RequestParam String nick_name,
                             @RequestParam String currentPwd,
                             @RequestParam(required = false) String introduce,
                             @RequestParam(required = false) String zipcode,
                             @RequestParam(required = false) String address,
                             @RequestParam(required = false) String addressDetail,
                             @RequestParam(required = false) MultipartFile file,
                             @RequestParam(required = false) String phone,
                             HttpServletRequest request,
                             Model model) throws IOException {

        User user = userDetails.getUser();
        HttpSession session = request.getSession();
        String verifiedPhone = (String) session.getAttribute("verifiedPhone");

        //  비밀번호 검증
        if (!service.checkPassword(user, currentPwd)) {
            model.addAttribute("errorMessage", "현재 비밀번호가 올바르지 않습니다.");
            model.addAttribute("user", user);
            return "user/edit";
        }

        //  일반 정보 수정 (전화번호 제외)
        user.setNick_name(nick_name);
        user.setEmail(email);
        user.setIntroduce(introduce);
        user.setZipcode(zipcode);
        user.setAddress(address);
        user.setAddressDetail(addressDetail);

        //  전화번호 인증 여부 확인 후 변경
        if (phone != null && !phone.isBlank()) {
            if (phone.equals(user.getPhone())) {
                // 같은 번호로 유지하는 경우 그대로 둠
            } else if (phone.equals(verifiedPhone)) {
                user.setPhone(phone); // 인증된 번호만 저장
                session.removeAttribute("verifiedPhone"); // 저장 후 제거
            } else {
                model.addAttribute("warningMessage", "전화번호는 인증된 번호만 변경할 수 있습니다.");
            }
        }

        service.updateUserWithFile(user.getId(), user, file);

        // 로그인 세션 갱신
        session.invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/user/login?updated=true";
    }


    @GetMapping("/user/password/change")
    public String showChangePasswordForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("user", userDetails.getUser());
        return "user/password-change";
    }

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

        user.setPwd(newPwd);
        service.update(user.getId(), user);

        request.getSession().invalidate();
        SecurityContextHolder.clearContext();

        return "redirect:/user/login?pw_changed=true";
    }

    @GetMapping("/user/password-reset")
    public String passwordResetForm() {
        return "user/password-reset";
    }

    @PostMapping("/user/password-reset")
    public String passwordResetSubmit(@RequestParam("email") String email, Model model) {
        try {
            User user = service.findByEmail(email);
            String tempPassword = UUID.randomUUID().toString().substring(0, 10);
            user.setPwd(tempPassword);
            service.update(user.getId(), user);
            emailService.sendTempPassword(email, tempPassword);
            model.addAttribute("message", "임시 비밀번호가 이메일로 발송되었습니다.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "해당 이메일로 가입된 사용자가 없습니다.");
        }
        return "user/password-reset";
    }

    @PostMapping("/user/withdraw")
    public String withdraw(@AuthenticationPrincipal CustomUserDetails userDetails,
                           HttpServletRequest request) {
        User user = userDetails.getUser();
        service.delete(user.getId());
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/user/login?withdrawn=true";
    }

    // 🔽 추가된 이메일 인증 관련 컨트롤러

    @GetMapping("/user/email/verify")
    public String emailVerifyPopup(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        model.addAttribute("email", userDetails.getUser().getEmail());
        return "user/email_verify";
    }

    @PostMapping("/user/email/send-code")
    public String sendEmailCode(@RequestParam String email, HttpSession session, Model model) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        session.setAttribute("emailCode", code);
        session.setAttribute("targetEmail", email);
        emailService.sendVerificationCode(email, code);
        model.addAttribute("email", email);
        model.addAttribute("success", "인증 코드가 전송되었습니다.");
        return "user/email_verify";
    }

    @PostMapping("/user/email/confirm")
    @ResponseBody
    public String confirmEmailCode(@RequestParam String code,
                                   @RequestParam String email,
                                   HttpSession session,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        String sessionCode = (String) session.getAttribute("emailCode");
        String targetEmail = (String) session.getAttribute("targetEmail");

        if (sessionCode != null && sessionCode.equals(code) && targetEmail.equals(email)) {
            User user = userDetails.getUser();
            service.updateEmailChk(user.getId()); 
            session.removeAttribute("emailCode");
            session.removeAttribute("targetEmail");
            return "<script>window.opener.location.reload(); window.close();</script>";
        } else {
            model.addAttribute("email", email);
            model.addAttribute("error", "인증 코드가 일치하지 않습니다.");
            return "<script>alert('인증 실패'); history.back();</script>";  // 혹은 인증 실패 HTML로 리턴
        }
    }

}
