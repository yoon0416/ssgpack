package com.ssgpack.ssgfc.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    @GetMapping("/admin/dashboard")
    public String dashboard() {
        return "admin/dashboard"; // templates/admin/dashboard.html
    }
}
