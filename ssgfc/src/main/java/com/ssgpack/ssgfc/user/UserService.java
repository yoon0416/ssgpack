package com.ssgpack.ssgfc.user;

import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    @Lazy
    private final PasswordEncoder passwordEncoder;

    // 🔐 로그인 인증 처리 (Spring Security가 호출)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 로그인 정보입니다."));
        return new CustomUserDetails(user); // ← 커스텀 UserDetails로 감싸서 리턴
    }

    // ✅ 회원가입 (비밀번호 암호화 + IP + 기본 권한)
    public User insertMember(User user) {
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setIp();         // IP 자동 저장
        user.setRole(5);      // 디폴트값 5 멤버
        return userRepository.save(user);
    }

    // ✅ 전체 유저 조회
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ✅ ID 기준 유저 조회
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + id));
    }

    // ✅ 이메일 기준 유저 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }

    // ✅ 유저 정보 수정 (비밀번호 암호화 포함)
    public User update(Long id, User updatedUser) {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());
        user.setPwd(passwordEncoder.encode(updatedUser.getPwd()));
        user.setIp(updatedUser.getIp());
        user.setRole(updatedUser.getRole());

        return user;
    }

    // ✅ 유저 삭제
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}
