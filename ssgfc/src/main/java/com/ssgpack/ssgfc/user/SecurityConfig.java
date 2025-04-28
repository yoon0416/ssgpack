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
                //  카카오/구글 로그인 경로 허용
                .antMatchers("/kakaologin", "/kakao", "/googlelogin", "/google")
                    .permitAll()

                // 관리자 대시보드
                .antMatchers("/admin/dashboard")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.USER_MANAGER.getRoleName(),
                        UserRole.PLAYER_MANAGER.getRoleName(),
                        UserRole.BOARD_MANAGER.getRoleName(),
                        UserRole.GAME_MANAGER.getRoleName()
                    )

                // 마스터 관리자
                .antMatchers("/admin/master/**")
                    .hasAuthority(UserRole.MASTER.getRoleName())

                // 유저 관리자
                .antMatchers("/admin/user/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.USER_MANAGER.getRoleName())

                // 선수 관리자
                .antMatchers("/admin/player/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.PLAYER_MANAGER.getRoleName())

                // 게시판 관리자
                .antMatchers("/admin/board/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.BOARD_MANAGER.getRoleName())

                // 경기 요약 관리자
                .antMatchers("/admin/review/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.GAME_MANAGER.getRoleName())

                // 경기 일정 관리자
                .antMatchers("/admin/game/**")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.GAME_MANAGER.getRoleName())

                // 투표 생성 (마스터 또는 게시판 관리자)
                .antMatchers("/vote/create")
                    .hasAnyAuthority(
                        UserRole.MASTER.getRoleName(),
                        UserRole.BOARD_MANAGER.getRoleName())

                // 투표 제출 (로그인만 필요)
                .antMatchers("/vote/submit")
                    .authenticated()

                // 투표 상세, 목록 (누구나 가능)
                .antMatchers("/vote/**")
                    .permitAll()

                // 마이페이지 (로그인 필요)
                .antMatchers("/user/mypage/**")
                    .authenticated()

                // 게시판 - 글쓰기/수정/삭제 (로그인 필요)
                .antMatchers("/board/write", "/board/edit/**", "/board/delete/**")
                    .authenticated()

                // 게시판 - 글 상세보기 (누구나 가능)
                .antMatchers("/board/view/**")
                    .permitAll()

                // 그 외 모든 요청 허용
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/user/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
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

    // 정적 자원 무시 설정
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .antMatchers("/css/**", "/js/**", "/images/**", "/lib/**");
    }

    // 비밀번호 인코더 설정
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 인증 매니저
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Dao 인증 제공자
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
