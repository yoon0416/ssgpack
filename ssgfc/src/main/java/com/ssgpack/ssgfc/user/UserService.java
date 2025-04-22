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

    //  로그인 인증 처리 (Spring Security가 호출)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 로그인 정보입니다."));
        return new CustomUserDetails(user); // ← 커스텀 UserDetails로 감싸서 리턴
    }

    //  회원가입 (비밀번호 암호화 + IP + 기본 권한)
    public User insertMember(User user) {
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setIp();         // IP 자동 저장
        user.setRole(5);      // 디폴트값 5 멤버
        return userRepository.save(user);
    }

    //  전체 유저 조회
    public List<User> findAll() {
        return userRepository.findAll();
    }

    //  ID 기준 유저 조회
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + id));
    }

    //  이메일 기준 유저 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }

    //유저정보 업데이트
    public User update(Long id, User updatedUser) {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());

        // 비밀번호가 비어있지 않으면만 변경
        if (updatedUser.getPwd() != null && !updatedUser.getPwd().isBlank()) {
            user.setPwd(passwordEncoder.encode(updatedUser.getPwd()));
        }

        user.setIp(updatedUser.getIp()); // 필요 시 유지 or 생략 가능
        user.setRole(updatedUser.getRole());

        return user;
    }


    //  유저 삭제
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
    
    //  마이페이지 정보 수정 (닉네임 / 비번만)
    public void updateUser(Long id, User updatedUser) {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());

        //  이메일이 바뀌었는지 확인
        String newEmail = updatedUser.getEmail().trim();
        if (!newEmail.equals(user.getEmail())) {
            //  이미 존재하는 이메일인지 확인
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }
            user.setEmail(newEmail);
        }

        //  비밀번호 입력된 경우에만 변경
        if (updatedUser.getPwd() != null && !updatedUser.getPwd().isBlank()) {
            user.setPwd(passwordEncoder.encode(updatedUser.getPwd()));
        }
    }



    
}
