package com.ssgpack.ssgfc.playergame;

import com.ssgpack.ssgfc.player.Player;
import com.ssgpack.ssgfc.player.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class PlayerGameController {

    private final PlayerRepository playerRepository;

    @GetMapping("/mygame/player-memory")
    public String memoryGame(Model model) {
        List<Player> allPlayers = playerRepository.findAll()
            .stream()
            .filter(p -> p.getImageUrl() != null && !p.getImageUrl().isBlank())
            .collect(Collectors.toList());

        Collections.shuffle(allPlayers);
        List<Player> selected = allPlayers.stream().limit(4).collect(Collectors.toList());

        // 👉 11명 → 22장 필요하므로 두 배로 추가
        List<Player> duplicated = new ArrayList<>();
        duplicated.addAll(selected);
        duplicated.addAll(selected);
        Collections.shuffle(duplicated);

        model.addAttribute("shuffledPlayers", duplicated);
        return "mygame/player-memory";
    }

}
