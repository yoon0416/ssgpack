package com.ssgpack.ssgfc.review;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/review-preview-crawl")
    public String previewCrawl(@RequestParam String gameUrl) {
        try {
            return reviewService.fetchPreviewSummary(gameUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ 미리보기 실패: " + e.getMessage();
        }
    }

    // ✅ gameUrl 직접 받아서 저장
    @GetMapping("/api/review-save")
    public String saveReview(@RequestParam String gameUrl, @RequestParam String summary) {
        try {
            reviewService.saveSummary(gameUrl, summary);
            return "✅ 저장 성공!";
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ 저장 실패: " + e.getMessage();
        }
    }
}