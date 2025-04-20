package com.ssgpack.ssgfc.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB에서 유저 찾기
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 로그인 정보입니다."));

        // 권한 리스트 생성
        List<GrantedAuthority> authorities = new ArrayList<>();

        // 사용자 role(int)을 기반으로 권한 문자열 얻기 (예: ROLE_MEMBER)
        String roleName = UserRole.fromCode(user.getRole()).getRoleName();
        authorities.add(new SimpleGrantedAuthority(roleName));

        // UserDetails 객체 반환 (Spring Security에서 인증에 사용됨)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(), // username
                user.getPwd(),   // password
                authorities      // 권한 목록
        );
    }
}
