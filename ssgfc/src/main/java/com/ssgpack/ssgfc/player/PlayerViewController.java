// PlayerViewController.java
package com.ssgpack.ssgfc.player;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PlayerViewController {

    private final PlayerCrawlingService playerCrawlingService;

    @GetMapping("/player/compare")
    public String playerCompare() {
        return "player/player_compare"; // ✅ 정답
    }

    
    // HTML로 선수 목록 보여주기 (→ templates/player/player.html)
    @GetMapping("/players")
    public String showPlayers(Model model) {
        List<Player> players = playerCrawlingService.findAllPlayers();
        model.addAttribute("players", players);
        return "player/player";  // ⇒ src/main/resources/templates/player/player.html
    }
    @GetMapping("/players/{pNo}")
    public String showPlayerDetail(@PathVariable String pNo, Model model) {
        model.addAttribute("pNo", pNo);
        return "player/player-detail";
    }
    
}
