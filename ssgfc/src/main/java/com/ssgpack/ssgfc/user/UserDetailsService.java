package com.ssgpack.ssgfc.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        com.ssgpack.ssgfc.user.User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 로그인 정보입니다."));

        // 권한 부여
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (user.getRole() == 0) {
            authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
        }

        return new User(user.getEmail(), user.getPwd(), authorities);
    }
}
