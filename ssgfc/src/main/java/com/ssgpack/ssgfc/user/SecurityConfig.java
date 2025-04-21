package com.ssgpack.ssgfc.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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

                // 🔐 마스터 관리자 전용
                .antMatchers("/admin/master/**")
                    .hasAuthority(UserRole.MASTER.getRoleName())

                // 🔐 유저 관리자
                .antMatchers("/admin/user/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.USER_MANAGER.getRoleName())

                // 🔐 선수 관리자
                .antMatchers("/admin/player/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.PLAYER_MANAGER.getRoleName())

                // 🔐 게시판 관리자
                .antMatchers("/admin/board/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.BOARD_MANAGER.getRoleName())

                // 🔐 경기 일정 관리자
                .antMatchers("/admin/game/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.GAME_MANAGER.getRoleName())

                // 👤 마이페이지 - 로그인된 사용자만
                .antMatchers("/user/mypage/**")
                    .authenticated()

                // ✏️ 게시판 - 글쓰기/수정/삭제는 로그인 필요
                .antMatchers("/board/write", "/board/edit/**", "/board/delete/**")
                    .authenticated()

                // ✅ 게시판 - 글 상세보기는 누구나 가능
                .antMatchers("/board/view/**")
                    .permitAll()

                // 🌐 그 외 모든 요청 허용
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/user/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/main", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login")
                .invalidateHttpSession(true)
                .permitAll()
            );

        return http.build();
    }

    // 💡 정적 자원은 보안 필터 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .antMatchers("/css/**", "/js/**", "/images/**", "/lib/**");
    }

    // 🔒 비밀번호 암호화 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔑 인증 매니저
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // 🧩 인증 제공자 설정
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
