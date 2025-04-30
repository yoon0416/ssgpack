/*
로그 만들기 전 처음 구조:
- 기본 Spring Security 필터 사용 + HTML form에 name="email" 설정
   (loginProcessingUrl 사용 안했지만 .formLogin()은 남아있었음)

- 문제 원인:
	Spring Security의 기본 로그인 필터는 name="username"만 인식함.
	name="email"은 무시되고 컨트롤러도 호출되지 않음 → email= 빈 값으로 로그인 시도

- 중간 해결 시도:
	form 구조 점검, 로그 확인, SecurityContext 직접 등록해도 필터가 가로채서 실패

- 최종 해결:
	.formLogin().disable() 적용 + @PostMapping("/user/login") 커스텀 로그인 컨트롤러 직접 구성 → 정상 작동

*/
package com.ssgpack.ssgfc.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // ✅ 테스트 시에는 CSRF 끔
            .formLogin().disable() // ✅ 기본 로그인 필터 제거 — 커스텀 로그인 컨트롤러 사용
            .authorizeRequests(authorize -> authorize
                .antMatchers("/css/**", "/js/**", "/images/**", "/lib/**").permitAll()
                .antMatchers("/kakaologin", "/kakao", "/googlelogin", "/google").permitAll()
                .antMatchers("/admin/dashboard").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.USER_MANAGER.getRoleName(),
                    UserRole.PLAYER_MANAGER.getRoleName(),
                    UserRole.BOARD_MANAGER.getRoleName(),
                    UserRole.GAME_MANAGER.getRoleName()
                )                
                .antMatchers("/vote/edit/**").access("@customSecurity.checkVerified(authentication)")
                .antMatchers("/admin/master/**").hasAuthority(UserRole.MASTER.getRoleName())
                .antMatchers("/admin/user/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.USER_MANAGER.getRoleName()
                )
                .antMatchers("/admin/player/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.PLAYER_MANAGER.getRoleName()
                )
                .antMatchers("/admin/playerstat/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.PLAYER_MANAGER.getRoleName()
                )
                .antMatchers("/admin/notifications").hasAnyAuthority(
                	    UserRole.MASTER.getRoleName(),
                	    UserRole.BOARD_MANAGER.getRoleName()
                	)
                .antMatchers("/admin/board/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.BOARD_MANAGER.getRoleName()
                )
                .antMatchers("/admin/review/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.GAME_MANAGER.getRoleName()
                )
                .antMatchers("/admin/schedule/**").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.GAME_MANAGER.getRoleName()
                )
                .antMatchers("/vote/create").hasAnyAuthority(
                    UserRole.MASTER.getRoleName(),
                    UserRole.BOARD_MANAGER.getRoleName()
                )
                .antMatchers("/comment/**")
                .access("@customSecurity.checkVerified(authentication)")
                .antMatchers("/board/**/comment/add")
                .access("@customSecurity.checkVerified(authentication)")
                .antMatchers("/board/write", "/board/edit/**", "/board/delete/**")
                .access("@customSecurity.checkVerified(authentication)")
                .antMatchers("/vote/submit").authenticated()
                .antMatchers("/vote/**").permitAll()
                .antMatchers("/user/mypage/**").authenticated()
                .antMatchers("/board/view/**").permitAll()
                .anyRequest().permitAll()
            )
            .exceptionHandling(exception -> exception
            	    .authenticationEntryPoint((request, response, authException) -> {
            	        response.sendRedirect("/user/login");
            	    })
            	    .accessDeniedHandler((request, response, accessDeniedException) -> {
            	        response.sendRedirect("/auth-required");
            	    })
            	)
            .oauth2Login(oauth2 -> oauth2
                .defaultSuccessUrl("/", true)
            )
            .logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login")
                .invalidateHttpSession(true)
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
