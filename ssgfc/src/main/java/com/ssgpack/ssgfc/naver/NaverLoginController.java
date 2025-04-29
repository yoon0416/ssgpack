package com.ssgpack.ssgfc.naver;

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
public class NaverLoginController {

    private final NaverLoginService naverLoginService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/naverlogin")
    public String naverLogin() {
        return "redirect:" + naverLoginService.step1();
    }

    @GetMapping("/naver")
    public String naverCallback(@RequestParam("code") String code) {
        Map<String, String> infos = naverLoginService.step2(code);

        String email = infos.get("email");
        String nickname = infos.get("name");
        String naverId = infos.get("id");

        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = new User();
            user.setEmail(email);
            user.setNick_name(nickname);
            user.setPwd(passwordEncoder.encode("1234"));
            user.setIp();
            user.setRole(5);
            user.setNaverId(naverId);
            userRepository.save(user);
        }

        UserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(token);

        return "redirect:/";
    }
}
