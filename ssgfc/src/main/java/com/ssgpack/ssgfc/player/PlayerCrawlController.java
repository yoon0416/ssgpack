package com.ssgpack.ssgfc.player;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class PlayerCrawlController {

    private final PlayerCrawlingService playerCrawlingService;

    @GetMapping("/player-crawl")
    public ResponseEntity<String> crawlAndSave() {
        int saved = playerCrawlingService.crawlAndSaveSSGPlayers();
        return ResponseEntity.ok("크롤링 완료! 저장된 선수 수: " + saved);
    }

}

/*
    @GetMapping("/player-crawl/")
    public String getCoachList() throws IOException {
        String apiUrl = "https://statiz.sporki.com/?team=NC&year=2023";

        // Jsoup을 HTTP 클라이언트로 사용 가능 (단순 텍스트 응답)
        Document doc = Jsoup.connect(apiUrl)
                            .ignoreContentType(true) // JSON 받아야하니까 꼭 써주기
                            .get();

        return doc.body().text(); // JSON 문자열 그대로 반환
    }
*/