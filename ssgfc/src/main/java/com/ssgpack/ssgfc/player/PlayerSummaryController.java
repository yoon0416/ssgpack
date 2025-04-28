package com.ssgpack.ssgfc.player;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PlayerSummaryController {

    private final PlayerSummaryService playerSummaryService;

    @GetMapping("/admin/player-summaries")
    public String generateSummaries() {
        playerSummaryService.generatePlayerSummaries();
        return "생성 완료";
    }
}