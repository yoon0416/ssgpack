package com.ssgpack.ssgfc.player;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerCrawlService crawlService;
    private final PlayerRepository playerRepository;

    // 1. 콘솔 출력용 (저장 X)
    @GetMapping("/api/player/print")
    public String printToConsoleOnly() {
        crawlService.crawlSSGPlayers();
        return "콘솔 출력 완료!";
    }

    // 2. 크롤링 + DB 저장
    @PostMapping("/api/player/crawl/ssg")
    public ResponseEntity<String> crawlAndSave() {
        try {
            List<Player> players = crawlService.crawlSSGPlayers();
            playerRepository.saveAll(players);
            return ResponseEntity.ok(players.size() + "명 저장 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("크롤링 실패: " + e.getMessage());
        }
    }

    // 3. DB에 저장된 선수 전체 조회
    @GetMapping("/api/players")
    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }
}
