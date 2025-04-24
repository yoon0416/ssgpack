package com.ssgpack.ssgfc.user;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // 로그인 인증 처리
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 로그인 정보입니다."));
        return new CustomUserDetails(user);
    }

    // 회원가입 처리 (비밀번호 암호화 + IP + 기본 권한 부여)
    public User insertMember(User user) {
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setIp();
        user.setRole(5);
        return userRepository.save(user);
    }

    // 전체 유저 조회
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ID로 유저 조회
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + id));
    }

    // 이메일로 유저 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }

    // 관리자 전용 유저 업데이트
    public User update(Long id, User updatedUser) {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());

        if (updatedUser.getPwd() != null && !updatedUser.getPwd().isBlank()) {
            user.setPwd(passwordEncoder.encode(updatedUser.getPwd()));
        }

        user.setIp(updatedUser.getIp());
        user.setRole(updatedUser.getRole());

        return user;
    }

    // 유저 삭제 (관리자용)
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    // 유저 탈퇴 (자기 자신 삭제용)
    public void deleteByUser(Long id) {
        userRepository.deleteById(id);
    }

    // 마이페이지 수정 (이메일 / 닉네임만 변경)
    public void updateUser(Long id, User updatedUser) {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());

        String newEmail = updatedUser.getEmail().trim();
        if (!newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }
            user.setEmail(newEmail);
        }

        // 비밀번호는 여기서 절대 수정하지 않음
    }

    // 마이페이지 수정 (현재 비밀번호 확인 포함 + 이메일/닉네임/비밀번호 모두 수정 가능)
    public void updateUserWithPasswordCheck(Long id, String currentPassword, User updatedUser) {
        User user = findById(id);

        if (!passwordEncoder.matches(currentPassword, user.getPwd())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.setNick_name(updatedUser.getNick_name());

        String newEmail = updatedUser.getEmail().trim();
        if (!newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }
            user.setEmail(newEmail);
        }

        String newPwd = updatedUser.getPwd();
        if (newPwd != null && !newPwd.trim().isEmpty()) {
            user.setPwd(passwordEncoder.encode(newPwd));
        }
    }

    // 키워드로 검색 및 페이징 처리
    public Page<User> getUserList(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchByKeyword(keyword, pageable);
    }

    // 비밀번호 일치 여부 확인
    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPwd());
    }

    /*
    // 더미 유저 자동 생성 (초기 개발 시 테스트용)
    @Bean
    public CommandLineRunner initDummyUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            for (int i = 1; i <= 30; i++) {
                String email = "dummy" + i + "@test.com";
                if (userRepository.findByEmail(email).isPresent()) continue;

                User user = new User();
                user.setEmail(email);
                user.setNick_name("유저" + i);
                user.setPwd(passwordEncoder.encode("1234"));
                user.setIp("127.0.0.1");
                user.setRole(5);
                userRepository.save(user);
            }
        };
    }
    */
}
