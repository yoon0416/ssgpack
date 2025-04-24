package com.ssgpack.ssgfc.player.stat;

import com.ssgpack.ssgfc.player.Player;
import com.ssgpack.ssgfc.player.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PlayerStatViewController {

    private final PlayerRepository playerRepository;
    private final PlayerStatRepository playerStatRepository;

    @GetMapping("/playerstats")
    public String showPlayerStats(Model model) {
        List<Player> players = playerRepository.findAll();
        model.addAttribute("players", players);

        List<PlayerStat> stats = playerStatRepository.findAll();
        model.addAttribute("playerStats", stats);

        return "player/player-stat"; // ⇒ templates/player/player-stat.html
    }
}
