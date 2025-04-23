package com.ssgpack.ssgfc.review;

import java.time.LocalDate;

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
}