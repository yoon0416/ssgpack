package com.ssgpack.ssgfc.game;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/crawl")
@RequiredArgsConstructor
public class SSGScheduleController {

    private final SSGScheduleCrawler crawler;

    @GetMapping("/ssg")
    public String crawlSsgSchedule() {
        crawler.crawlAndSave();
        return "✅ SSG 경기 스케줄 크롤링 및 저장 완료!";
    }
}
