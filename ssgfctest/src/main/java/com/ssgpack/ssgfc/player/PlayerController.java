package com.ssgpack.ssgfc.player;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerCrawlService crawlService;
    private final PlayerRepository playerRepository;

    // 특정 선수 크롤링 후 저장
    @PostMapping("/api/player/{id}/crawl")
    public ResponseEntity<String> crawlAndSave(@PathVariable String id) {
        try {
            Player player = crawlService.crawlPlayer(id);
            playerRepository.save(player);
            return ResponseEntity.ok("선수 저장 완료: " + player.getName());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("에러 발생: " + e.getMessage());
        }
    }

    // 저장된 선수 전체 보기
    @GetMapping("/api/players")
    public List<Player> getPlayers() {
        return playerRepository.findAll();
    }
}
