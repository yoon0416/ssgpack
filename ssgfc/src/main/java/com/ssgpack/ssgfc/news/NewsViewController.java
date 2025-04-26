package com.ssgpack.ssgfc.news;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NewsViewController {
    @GetMapping("/news")
    public String newsPage() {
        return "news";  // templates/news.html을 반환함
    }
}
