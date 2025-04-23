package com.ssgpack.ssgfc.admin;

import com.ssgpack.ssgfc.user.User;
import com.ssgpack.ssgfc.user.UserRepository;
import com.ssgpack.ssgfc.user.UserService;
import com.ssgpack.ssgfc.user.CustomUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/user")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    // 🔹 유저 리스트 (검색 + 페이징)
    @GetMapping("/list")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       Model model) {
        int pageSize = 10;

        Page<User> userPage = userService.getUserList(keyword, PageRequest.of(page, pageSize, Sort.by("id").descending()));

        model.addAttribute("userList", userPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());

        return "admin/user/list";
    }

    // 🔹 유저 상세 보기
    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/user/view";
        }
        return "redirect:/admin/user/list";
    }

    // 🔹 유저 수정 폼
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/user/edit";
        }
        return "redirect:/admin/user/list";
    }

    // 🔹 유저 수정 처리
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute User user,
                             @AuthenticationPrincipal CustomUserDetails currentUser) {
        User target = userService.findById(id);

        if (target.getRole() == 0 && currentUser.getUser().getRole() != 0) {
            return "redirect:/admin/user/list?error=unauthorized";
        }

        userService.update(id, user);
        return "redirect:/admin/user/view/" + id;
    }

    // 🔹 유저 삭제
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/user/list";
    }
}
