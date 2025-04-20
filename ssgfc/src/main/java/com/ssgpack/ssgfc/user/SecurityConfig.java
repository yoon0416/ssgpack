package com.ssgpack.ssgfc.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorize -> authorize

                // 마스터 관리자만 접근 가능한 경로
                .antMatchers("/admin/master/**").hasAuthority(UserRole.MASTER.getRoleName())

                // 유저 관리자 또는 마스터 관리자 접근 가능
                .antMatchers("/admin/user/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.USER_MANAGER.getRoleName())

                // 선수 관리자 또는 마스터 관리자 접근 가능
                .antMatchers("/admin/player/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.PLAYER_MANAGER.getRoleName())

                // 게시판 관리자 또는 마스터 관리자 접근 가능
                .antMatchers("/admin/board/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.BOARD_MANAGER.getRoleName())

                // 경기일정 관리자 또는 마스터 관리자 접근 가능
                .antMatchers("/admin/game/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.GAME_MANAGER.getRoleName())

                // 로그인한 사용자만 마이페이지 접근 가능 (모든 관리자, 일반유저 포함)
                .antMatchers("/user/mypage/**").authenticated()

                // 그 외 모든 요청은 허용
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                // 로그인 페이지 경로
                .loginPage("/user/login")

                // 로그인 요청 처리 경로 (POST)
                .loginProcessingUrl("/login")

                // 로그인 성공 시 항상 /main으로 이동
                .defaultSuccessUrl("/main", true)

                // 로그인 페이지 및 요청은 누구나 접근 가능
                .permitAll()
            )
            .logout(logout -> logout
                // 로그아웃 처리 경로
                .logoutUrl("/user/logout")

                // 로그아웃 성공 시 이동할 경로
                .logoutSuccessUrl("/user/login")

                // 세션 무효화
                .invalidateHttpSession(true)

                // 로그아웃은 누구나 접근 가능
                .permitAll()
            );

        return http.build();
    }

    // 정적 자원(css, js, 이미지)은 시큐리티 필터 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**", "/images/**", "/lib/**");
    }

    // 비밀번호 암호화 설정 (BCrypt 사용)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 매니저 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
