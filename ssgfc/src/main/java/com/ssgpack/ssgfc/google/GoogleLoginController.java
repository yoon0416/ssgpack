package com.ssgpack.ssgfc.google;

import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class GoogleLoginController {

    private final GoogleLoginService googleLoginService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Step 1: 구글 로그인 버튼 클릭 시 구글 로그인 URL로 이동
    @GetMapping("/googlelogin")
    public String googleLogin() {
        return "redirect:" + googleLoginService.step1();
    }

    // Step 2: 구글 콜백 처리 (code 받아오기)
    @GetMapping("/google")
    public String googleCallback(@RequestParam("code") String code) {
        Map<String, String> infos = googleLoginService.step2(code);

        String email = infos.get("email");
        String nickname = infos.get("name");
        String googleId = infos.get("id"); 

        // 이메일로 기존 유저 찾기
        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            // 신규 유저 가입
            user = new User();
            user.setEmail(email);
            user.setNick_name(nickname);
            user.setPwd(passwordEncoder.encode("1234")); //  비번 1234로 고정
            user.setIp();
            user.setRole(5);
            user.setGoogleId(googleId); 
            userRepository.save(user);
        }

        // 로그인 처리 (Spring Security 세션 주입)
        UserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);

        return "redirect:/";
    }
}
