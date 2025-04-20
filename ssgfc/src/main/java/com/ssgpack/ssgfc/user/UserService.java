package com.ssgpack.ssgfc.user;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 (비밀번호 암호화 + IP + 기본 권한 + username 생성)
    public User insertMember(User user) {
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setIp();          // IP 자동 저장
        user.setRole(5);       // 디폴트값 5: 일반 멤버

        // ✅ username 자동 생성 (중복 방지를 위해 UUID 일부 사용)
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            user.setUsername("user_" + uuid);
        }

        return userRepository.save(user);
    }

    // 전체 유저 조회
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ID 기준 유저 조회
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + id));
    }

    // 이메일 기준 유저 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }

    // 유저 정보 수정 (비밀번호 암호화 포함)
    public User update(Long id, User updatedUser) {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());
        user.setPwd(passwordEncoder.encode(updatedUser.getPwd()));
        user.setIp(updatedUser.getIp());
        user.setRole(updatedUser.getRole());

        // 🔥 선택: username은 기본적으로 수정 안 함 (원하면 여기 추가 가능)

        return user;
    }

    // 유저 삭제
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. username: " + username));
    }

}
