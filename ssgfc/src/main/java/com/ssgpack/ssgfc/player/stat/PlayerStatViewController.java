package com.ssgpack.ssgfc.player.stat;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ssgpack.ssgfc.player.Player;
import com.ssgpack.ssgfc.player.PlayerRepository;

import lombok.RequiredArgsConstructor;

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
