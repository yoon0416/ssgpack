package com.ssgpack.ssgfc.admin;

import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;
import com.ssgpack.ssgfc.user.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // 🔹 유저 리스트 페이지
    @GetMapping("/list")
    public String list(Model model) {
        model.addAttribute("userList", userService.findAll());
        return "admin/user/list"; // templates/admin/user/list.html
    }

    // 🔹 유저 상세 보기
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/user/view"; // templates/admin/user/view.html
        }
        return "redirect:/admin/user/list";
    }

    // 🔹 유저 수정 폼
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/user/edit"; // templates/admin/user/edit.html
        }
        return "redirect:/admin/user/list";
    }

    // 🔹 유저 수정 처리 (닉네임, 권한, 비밀번호)
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
                             @AuthenticationPrincipal CustomUserDetails currentUser) {

        User target = userService.findById(id);

        //  마스터 계정은 마스터만 수정 가능
        if (target.getRole() == 0 && currentUser.getUser().getRole() != 0) {
            return "redirect:/admin/user/list?error=unauthorized";
        }

        userService.update(id, user);
        return "redirect:/admin/user/view/" + id;
    }


    // 🔹 유저 삭제 처리
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/user/list";
    }
}
