package com.ssgpack.ssgfc.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // URL 보안 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll() // 모든 URL 허용
            )
            .formLogin((formLogin) -> formLogin
                .loginPage("/user/login")              // 🔐 커스텀 로그인 페이지 경로
                .loginProcessingUrl("/login")          // 🔐 로그인 form action 주소
                .defaultSuccessUrl("/main", true)      // 🔐 로그인 성공 시 무조건 /main 이동
            )
            .logout((logout) -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout")) // 로그아웃 URL
                .logoutSuccessUrl("/user/login")       // 로그아웃 후 이동할 경로
                .invalidateHttpSession(true)           // 세션 무효화
            );

        return http.build();
    }

    // 비밀번호 암호화 (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 로그인 인증 처리 매니저
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
