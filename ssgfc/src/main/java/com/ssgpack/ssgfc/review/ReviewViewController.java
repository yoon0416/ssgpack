package com.ssgpack.ssgfc.review;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class ReviewViewController {

    @GetMapping("/review_today")
    public String showReviewToday(@RequestParam(required = false) String date, Model model) {
        // 날짜 파라미터가 없는 경우 오늘 날짜로 리디렉션
        if (date == null || date.isBlank()) {
            date = LocalDate.now().toString(); // yyyy-MM-dd
            return "redirect:/review_today?date=" + date;
        }

        // 템플릿에서 selectedDate를 직접 쓸 수 있게 모델에 담기
        model.addAttribute("selectedDate", date);

        return "review/review_today"; 
    }
    @GetMapping("/admin/review")
    public String adminReviewPage() {
        return "admin/review/review";  
    }
}