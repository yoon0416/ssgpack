package com.ssgpack.ssgfc.news;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {


    private final NaverNewsService newsService;

    @GetMapping("/baseball")
    public Map<String, Object> getBaseballNews() {
        return newsService.getNews("KBO"); // ← 큰따옴표 넣으면 더 정확해짐
    }
    

}