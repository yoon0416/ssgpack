package com.ssgpack.ssgfc.player.stat;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PlayerStatViewController {

    private final PlayerStatCrawlingService playerStatCrawlingService;

    @GetMapping("/playerstats")
    public String showPlayerStats(Model model) {
        List<PlayerStat> stats = playerStatCrawlingService.findAllStats();
        model.addAttribute("playerStats", stats);
        return "player/player-stat";  // ⇒ templates/player/player-stat.html
    }
}
