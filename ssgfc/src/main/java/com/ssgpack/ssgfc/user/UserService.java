package com.ssgpack.ssgfc.user;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.ssgpack.ssgfc.log.LogUtil;
import com.ssgpack.ssgfc.util.UtilUpload;

import lombok.RequiredArgsConstructor;

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
        LogUtil.write("user", "[LOGIN_TRY] email=" + email);

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> {
                    LogUtil.write("user", "[LOGIN_FAIL] email=" + email + ", reason=NOT_FOUND");
                    return new UsernameNotFoundException("잘못된 로그인 정보입니다.");
                });

        LogUtil.write("user", "[LOGIN_SUCCESS] email=" + email);
        return new CustomUserDetails(user);
    }

    public User insertMember(User user) {
        user.setPwd(passwordEncoder.encode(user.getPwd()));
        user.setIp();
        user.setRole(5);
        User saved = userRepository.save(user);
        LogUtil.write("user", "[SIGNUP] email=" + saved.getEmail());
        return saved;
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

        if (updatedUser.getRole() != user.getRole()) {
            user.setRole(updatedUser.getRole());
            LogUtil.write("user", "[ROLE_CHANGED] userId=" + id + ", newRole=" + updatedUser.getRole());
        }

        LogUtil.write("user", "[UPDATE_BASIC_INFO] userId=" + id);
        return user;
    }

    // 마이페이지 수정 - 소개글, 프사, 주소 포함
    public void updateUserWithFile(Long id, User updatedUser, MultipartFile file) throws IOException {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());
        user.setIntroduce(updatedUser.getIntroduce());
        user.setPhone(updatedUser.getPhone());

        String newEmail = updatedUser.getEmail().trim();
        if (!newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                LogUtil.write("user", "[EMAIL_UPDATE_FAIL] reason=duplicate, tried=" + newEmail);
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }
            user.setEmail(newEmail);
            user.setEmail_chk(false);
            LogUtil.write("user", "[EMAIL_CHANGED] userId=" + id + ", newEmail=" + newEmail);
        }

        user.setZipcode(updatedUser.getZipcode());
        user.setAddress(updatedUser.getAddress());
        user.setAddressDetail(updatedUser.getAddressDetail());

        if (file != null && !file.isEmpty()) {
            String savedName = utilUpload.uploadUserProfile(file);
            user.setProfile_img(savedName);
            LogUtil.write("user", "[PROFILE_IMAGE_UPDATED] userId=" + id + ", fileName=" + savedName);
        }

        userRepository.save(user); 
        LogUtil.write("user", "[MYPAGE_UPDATED] userId=" + id);
    }

    public void updateUser(Long id, User updatedUser) {
        User user = findById(id);

        user.setNick_name(updatedUser.getNick_name());

        String newEmail = updatedUser.getEmail().trim();
        if (!newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                LogUtil.write("user", "[EMAIL_UPDATE_FAIL] reason=duplicate, tried=" + newEmail);
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }
            user.setEmail(newEmail);
            user.setEmail_chk(false);
            LogUtil.write("user", "[EMAIL_CHANGED] userId=" + id + ", newEmail=" + newEmail);
        }

        LogUtil.write("user", "[MYPAGE_UPDATED_NO_FILE] userId=" + id);
    }

    public void updateUserWithPasswordCheck(Long id, String currentPassword, User updatedUser) {
        User user = findById(id);

        if (!passwordEncoder.matches(currentPassword, user.getPwd())) {
            LogUtil.write("user", "[PASSWORD_UPDATE_FAIL] userId=" + id + ", reason=wrong_current_password");
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.setNick_name(updatedUser.getNick_name());

        String newEmail = updatedUser.getEmail().trim();
        if (!newEmail.equals(user.getEmail())) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                LogUtil.write("user", "[EMAIL_UPDATE_FAIL] reason=duplicate, tried=" + newEmail);
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }
            user.setEmail(newEmail);
            user.setEmail_chk(false);
            LogUtil.write("user", "[EMAIL_CHANGED] userId=" + id + ", newEmail=" + newEmail);
        }

        String newPwd = updatedUser.getPwd();
        if (newPwd != null && !newPwd.trim().isEmpty()) {
            user.setPwd(passwordEncoder.encode(newPwd));
            LogUtil.write("user", "[PASSWORD_UPDATED] userId=" + id);
        }

        LogUtil.write("user", "[MYPAGE_UPDATED_WITH_PASSWORD] userId=" + id);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
        LogUtil.write("user", "[ACCOUNT_DELETED_BY_ADMIN] userId=" + id);
    }

    public void deleteByUser(Long id) {
        // 여기에 연관 데이터 삭제 로직 삽입 가능
        LogUtil.write("user", "[ACCOUNT_DATA_CLEANUP] userId=" + id + " (board, comment, vote 삭제 예정)");

        userRepository.deleteById(id);
        LogUtil.write("user", "[ACCOUNT_SELF_DELETED] userId=" + id);
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
        userRepository.save(user);
        LogUtil.write("user", "[EMAIL_VERIFIED] userId=" + id);
    }
    public User login(String email, String rawPassword) {
        LogUtil.write("user", "[LOGIN_TRY] email=" + email);

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> {
                    LogUtil.write("user", "[LOGIN_FAIL] email=" + email + ", reason=NOT_FOUND");
                    throw new UsernameNotFoundException("존재하지 않는 이메일입니다.");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPwd())) {
            LogUtil.write("user", "[LOGIN_FAIL] email=" + email + ", reason=WRONG_PASSWORD");
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        LogUtil.write("user", "[LOGIN_SUCCESS] email=" + email);
        return user;
    }
    public List<String> getBadgeList(User user) {
        List<String> badges = new ArrayList<>();

        if (user.getKakaoId() != null) {
            badges.add("카카오 로그인");
        }
        if (user.getGoogleId() != null) {
            badges.add("구글 로그인");
        }
        if (user.getNaverId() != null) {
            badges.add("네이버 로그인");
        }
        if (user.isEmail_chk()) {
            badges.add("이메일 인증");
        }
        if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
            badges.add("전화번호 인증");
        }

        return badges;
    }

}
