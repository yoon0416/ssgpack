package com.ssgpack.ssgfc.player;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
///////////////////////////////////
//나중에 없애기
///////////////////////////////////
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/player-crawl/**").permitAll() // 여기에 예외 추가 ✅
                .anyRequest().authenticated()
            .and()
            .formLogin() // 기본 로그인 폼 사용
                .loginPage("/login") // 커스텀 로그인 페이지 있으면 설정
                .permitAll()
            .and()
            .logout()
                .permitAll();

        return http.build();
    }
}
