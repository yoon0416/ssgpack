package com.ssgpack.ssgfc.user;

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
        // 로그인 시 입력된 email로 조회
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 로그인 정보입니다."));

        return new CustomUserDetails(user);  // ✅ 커스텀 UserDetails 반환
    }
}
