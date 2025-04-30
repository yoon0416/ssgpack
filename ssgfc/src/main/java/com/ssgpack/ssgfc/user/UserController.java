package com.ssgpack.ssgfc.user;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
    public String user(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) throw new UsernameNotFoundException("유저 없음");

        User user = userDetails.getUser();  // 👉 바로 user 꺼내면 됨

        String roleName;
        switch (user.getRole()) {
            case 0: roleName = "마스터 관리자"; break;
            case 1: roleName = "유저 관리자"; break;
            case 2: roleName = "선수 관리자"; break;
            case 3: roleName = "게시판 관리자"; break;
            case 4: roleName = "경기일정 관리자"; break;
            case 5: roleName = "일반 사용자"; break;
            default: roleName = "알 수 없음"; break;
        }
        List<String> badges = service.getBadgeList(user);
        
        model.addAttribute("user", user);
        model.addAttribute("roleName", roleName);
        model.addAttribute("badges", badges); 
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

        if (!service.checkPassword(user, currentPwd)) {
            model.addAttribute("errorMessage", "현재 비밀번호가 올바르지 않습니다.");
            model.addAttribute("user", user);
            return "user/edit";
        }

        try {
            String oldEmail = user.getEmail();

            user.setNick_name(nick_name);
            user.setIntroduce(introduce);
            user.setZipcode(zipcode);
            user.setAddress(address);
            user.setAddressDetail(addressDetail);
            user.setEmail(email.trim());

            if (!oldEmail.equals(email.trim())) {
                user.setEmail_chk(false); // 이메일 변경시 인증 다시 필요
            }

            if (phone != null && !phone.isBlank()) {
                if (phone.equals(user.getPhone())) {
                    // 기존 번호 유지
                } else if (phone.equals(verifiedPhone)) {
                    user.setPhone(phone);
                    session.removeAttribute("verifiedPhone"); // 인증 성공했으면 세션값 삭제
                } else {
                    model.addAttribute("warningMessage", "전화번호는 인증된 번호만 변경할 수 있습니다.");
                }
            }

            // 유저 정보 파일 포함 업데이트
            service.updateUserWithFile(user.getId(), user, file, phone);

            // ✨ 세션 무효화 제거
            // session.invalidate();
            // SecurityContextHolder.clearContext();

            // ✨ 인증정보 갱신
            User refreshedUser = service.findById(user.getId()); // ✅ DB에서 다시 조회
            CustomUserDetails updatedUserDetails = new CustomUserDetails(refreshedUser);
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails, 
                    updatedUserDetails.getPassword(), 
                    updatedUserDetails.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(newAuth);


            // 수정 후 마이페이지로 이동
            return "redirect:/user/mypage";

        } catch (IllegalArgumentException e) {
            model.addAttribute("message", e.getMessage());
            model.addAttribute("url", "/user/mypage/edit");
            return "user/alert";
        }
        
    }

    @GetMapping("/user/password/change")
    public String showChangePasswordForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("user", userDetails.getUser());
        return "user/password-change";
    }
    
    @PostMapping("/user/login")
    public String customLogin(@RequestParam("email") String email,
                              @RequestParam("password") String password,
                              HttpServletRequest request,
                              Model model) {
        try {
        	
            System.out.println("📥 email = " + email);
            System.out.println("📥 password = " + password);
        	
            User user = service.login(email, password);

            CustomUserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // 🔥 SecurityContext 생성 & 등록
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // 🔥 세션에도 반드시 수동 등록
            HttpSession session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);

            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/login";
        }
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
        } catch (IllegalArgumentException e) {
            // 무시 (존재하지 않는 이메일일 때)
        }
        model.addAttribute("message", "임시 비밀번호가 이메일로 발송되었습니다.");
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

            // ✅ 세션 정보 최신화
            User refreshedUser = service.findById(user.getId());
            CustomUserDetails updatedUserDetails = new CustomUserDetails(refreshedUser);
            UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                    updatedUserDetails,
                    updatedUserDetails.getPassword(),
                    updatedUserDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            session.removeAttribute("emailCode");
            session.removeAttribute("targetEmail");

            return "<script>window.opener.location.reload(); window.close();</script>";
        } else {
            return "<script>alert('인증 실패'); history.back();</script>";
        }
    }

}
