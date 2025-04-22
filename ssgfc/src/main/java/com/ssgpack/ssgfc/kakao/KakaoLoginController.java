package com.ssgpack.ssgfc.kakao;

import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;
import com.ssgpack.ssgfc.user.CustomUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class KakaoLoginController {

    private final KakaoLoginService kakaoLoginService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Step 1: 카카오 로그인 버튼 클릭 시 카카오 인증 URL로 리다이렉트
    @GetMapping("/kakaologin")
    public String kakaoLogin() {
        return "redirect:" + kakaoLoginService.step1();
    }

    // Step 2: 카카오로부터 code 받기 → 사용자 정보 조회 후 로그인 처리
    @GetMapping("/kakao")
    public String kakaoCallback(@RequestParam("code") String code) {
        List<String> infos = kakaoLoginService.step2(code);

        String nickname = infos.get(0);   // 사용자 닉네임
        String email = infos.get(2);      // 사용자 이메일 (없을 수도 있음)
        Long kakaoId = Long.parseLong(infos.get(3)); // 사용자 고유 ID

        // 먼저 kakaoId 중복 체크 → 이미 가입된 사용자라면 로그인 처리만 수행
        Optional<User> userByKakaoId = userRepository.findByKakaoId(kakaoId);
        if (userByKakaoId.isPresent()) {
            User user = userByKakaoId.get();
            UserDetails userDetails = new CustomUserDetails(user);
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(token);
            return "redirect:/main";
        }

        // 이메일이 없거나 중복된 경우, 임시 이메일 생성
        if (email == null || email.trim().isEmpty() || userRepository.findByEmail(email).isPresent()) {
            int index = 1;
            String tempEmail;
            do {
                tempEmail = "kakaouser" + index + "@kakao.com";
                index++;
            } while (userRepository.findByEmail(tempEmail).isPresent());
            email = tempEmail;
        }

        // 새 사용자 등록
        User user = new User();
        user.setEmail(email);
        user.setNick_name(nickname);
        user.setPwd(passwordEncoder.encode("kakao_oauth"));
        user.setKakaoId(kakaoId);
        user.setIp();
        user.setRole(5);
        userRepository.save(user);

        // Spring Security 인증 객체 설정 → 자동 로그인
        UserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);

        return "redirect:/main";
    }
}