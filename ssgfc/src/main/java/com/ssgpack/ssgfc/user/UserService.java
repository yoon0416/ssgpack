package com.ssgpack.ssgfc.user;

import com.ssgpack.ssgfc.util.UtilUpload;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UtilUpload utilUpload;

    @Lazy
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("잘못된 로그인 정보입니다."));
        return new CustomUserDetails(user);
    }

    public User insertMember(User user) {
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setIp();
        user.setRole(5);
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다. ID: " + id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));
    }

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

    // ✅ 마이페이지 수정 - 소개글, 프사, 주소 포함
    public void updateUserWithFile(Long id, User updatedUser, MultipartFile file) throws IOException {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());
        user.setIntroduce(updatedUser.getIntroduce());
        user.setPhone(updatedUser.getPhone());

        // 이메일 변경
        String newEmail = updatedUser.getEmail().trim();
        if (!newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }
            user.setEmail(newEmail);
        }

        // ✅ 주소 정보 저장
        user.setZipcode(updatedUser.getZipcode());
        user.setAddress(updatedUser.getAddress());
        user.setAddressDetail(updatedUser.getAddressDetail());

        // 프로필 이미지 처리
        if (file != null && !file.isEmpty()) {
            String savedName = utilUpload.uploadUserProfile(file);
            user.setProfile_img(savedName);
        }
    }

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
    }

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

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public void deleteByUser(Long id) {
        userRepository.deleteById(id);
    }

    public Page<User> getUserList(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.searchByKeyword(keyword, pageable);
    }

    public boolean checkPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPwd());
    }
    public void updateEmailChk(Long id) {
        User user = findById(id);
        user.setEmail_chk(true);
        userRepository.save(user); // 꼭 save 해야 DB에 반영됨
    }

}
