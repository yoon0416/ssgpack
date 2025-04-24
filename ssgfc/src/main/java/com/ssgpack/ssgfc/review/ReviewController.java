package com.ssgpack.ssgfc.review;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/api/review-exists")
    public boolean checkReviewExists(@RequestParam String date) {
        return reviewService.existsByDate(date);
    }
    @GetMapping("/api/review-summary")
    public String getSummaryByDate(@RequestParam String date) {
        return reviewService.findSummaryByDate(LocalDate.parse(date));
    }
    @GetMapping("/api/review-records")
    public List<Review> getRecordsByDate(@RequestParam String date) {
        LocalDate localDate = LocalDate.parse(date);
        return reviewService.findByGameDate(localDate);
    }
    @GetMapping("/api/review-crawl")
    public String crawlReviewManually(@RequestParam String gameUrl) {
        try {
            reviewService.fetchAndSaveReview(gameUrl);
            return "✅ 크롤링 및 저장 성공: " + gameUrl;
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ 크롤링 실패: " + e.getMessage();
        }
    }
    
}