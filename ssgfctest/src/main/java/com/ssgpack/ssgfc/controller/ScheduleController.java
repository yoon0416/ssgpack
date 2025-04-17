package com.ssgpack.ssgfc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssgpack.ssgfc.service.ScheduleCrawlerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleCrawlerService crawlerService;

    @GetMapping("/crawl")
    public String crawl() {
        crawlerService.crawlAndSave();
        return "크롤링 완료!";
    }
}
