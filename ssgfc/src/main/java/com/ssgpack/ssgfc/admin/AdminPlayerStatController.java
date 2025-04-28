package com.ssgpack.ssgfc.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssgpack.ssgfc.player.Player;
import com.ssgpack.ssgfc.player.PlayerRepository;
import com.ssgpack.ssgfc.player.stat.PlayerStat;
import com.ssgpack.ssgfc.player.stat.PlayerStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin/playerstat")
@RequiredArgsConstructor
public class AdminPlayerStatController {

    private final PlayerStatRepository playerStatRepository;
    private final PlayerRepository playerRepository;
    private final ObjectMapper objectMapper;

    // 기록 추가 폼 (플레이어 상세에서 "기록 추가" 버튼 클릭 시)
    @GetMapping("/create")
    public String createForm(@RequestParam("playerId") Long playerId, Model model) {
        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid player id: " + playerId));
        model.addAttribute("player", player);
        return "admin/playerstat/create";
    }

    // 기록 저장 처리 후 플레이어 상세로 리다이렉트
    @PostMapping("/create")
    public String create(
        @RequestParam Long playerId,
        @RequestParam String season,
        @RequestParam("statMap") String statMapRaw
    ) throws Exception {
        String asJson = statMapRaw
            .replaceAll("(?<=\\{|,)(\\s*)([^=,\\s]+)(\\s*)=", "\"$2\":")
            .replaceAll(":(?!\\{)([^,\\}]+)(?=,|\\})", ":\"$1\"");

        Map<String,String> statMap = objectMapper.readValue(
            asJson, new TypeReference<Map<String,String>>() {}
        );

        Player player = playerRepository.findById(playerId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid player id: " + playerId));
        PlayerStat stat = PlayerStat.builder()
            .player(player)
            .season(season)
            .statMap(statMap)
            .build();
        playerStatRepository.save(stat);

        return "redirect:/admin/player/view/" + playerId;
    }

    // 기록 수정 폼 (플레이어 상세에서 "기록 수정" 버튼 클릭 시)
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        PlayerStat stat = playerStatRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid stat id: " + id));
        model.addAttribute("stat", stat);
        model.addAttribute("player", stat.getPlayer());
        return "admin/playerstat/edit";
    }

    // 기록 수정 처리 후 플레이어 상세로 리다이렉트
    @PostMapping("/edit/{id}")
    public String edit(
        @PathVariable Long id,
        @RequestParam String season,
        @RequestParam("statMap") String statMapRaw
    ) throws Exception {
        PlayerStat existing = playerStatRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid stat id: " + id));

        String asJson = statMapRaw
            .replaceAll("(?<=\\{|,)(\\s*)([^=,\\s]+)(\\s*)=", "\"$2\":")
            .replaceAll(":(?!\\{)([^,\\}]+)(?=,|\\})", ":\"$1\"");

        Map<String,String> statMap = objectMapper.readValue(
            asJson, new TypeReference<Map<String,String>>() {}
        );

        PlayerStat updated = PlayerStat.builder()
            .id(existing.getId())
            .player(existing.getPlayer())
            .season(season)
            .statMap(statMap)
            .build();
        playerStatRepository.save(updated);

        Long playerId = existing.getPlayer().getId();
        return "redirect:/admin/player/view/" + playerId;
    }

    // 기록 삭제 처리 후 플레이어 상세로 리다이렉트
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        PlayerStat stat = playerStatRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid stat id: " + id));
        Long playerId = stat.getPlayer().getId();
        playerStatRepository.deleteById(id);
        return "redirect:/admin/player/view/" + playerId;
    }
}
