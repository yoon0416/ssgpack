// 유저 서비스	
package com.ssgpack.ssgfc.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    // 사용자 전체 조회
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // ID로 조회
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + id));
    }

    // 이메일로 조회
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }

    // 유저 저장 (회원가입 등)
    public User save(User user) {
        return userRepository.save(user);
    }

    // 유저 수정
    public User update(Long id, User updatedUser) {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());
        user.setPwd(updatedUser.getPwd());
        user.setIp(updatedUser.getIp());

        return user;
    }

    // 유저 삭제
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
}