package com.ssgpack.ssgfc.report;

import com.ssgpack.ssgfc.user.CustomUserDetails;
import com.ssgpack.ssgfc.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final ReportService reportService;

    // ✅ 관리자 알림 목록 + 미처리 신고 목록 조회
    @GetMapping
    public String getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User admin = userDetails.getUser();
        model.addAttribute("notifications", notificationService.getNotifications(admin));
        model.addAttribute("reports", reportService.findUnprocessedReports(0));
        return "admin/notification/list";
    }
}
