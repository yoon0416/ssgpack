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
                .antMatchers("/vote/submit").authenticated()
                .antMatchers("/vote/**").permitAll()
                .antMatchers("/user/mypage/**").authenticated()
                .antMatchers("/board/write", "/board/edit/**", "/board/delete/**").authenticated()
                .antMatchers("/board/view/**").permitAll()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/user/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
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
