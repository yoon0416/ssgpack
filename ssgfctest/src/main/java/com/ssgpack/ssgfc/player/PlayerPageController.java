package com.ssgpack.ssgfc.player;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PlayerPageController {

    private final PlayerRepository playerRepository;
    private final PlayerCrawlService crawlService; // ✅ 크롤링 서비스 주입

    @GetMapping("/players")
    public String showPlayerList(Model model) {
        model.addAttribute("players", playerRepository.findAll());
        return "player-list"; // resources/templates/player-list.html
    }

    @PostMapping("/api/players/crawl")
    public ResponseEntity<String> crawlAllPlayers() {
        try {
            List<Player> players = crawlService.crawlSSGPlayers(); // ✅ 메서드 이름도 맞춰줘
            playerRepository.saveAll(players);
            return ResponseEntity.ok(players.size() + "명의 선수 저장 완료");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("에러 발생: " + e.getMessage());
        }
    }
}