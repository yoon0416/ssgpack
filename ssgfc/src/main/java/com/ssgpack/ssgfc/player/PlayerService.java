package com.ssgpack.ssgfc.player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ssgpack.ssgfc.player.stat.PlayerStat;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    // ⬆️ 조회수 기준 상위 선수
    public List<Player> getTopPlayersByViewCount(int limit) {
        return playerRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Player::getViewCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ⬆️ mainStat(비율 or WAR) 기준 상위 선수 ("season"이 통산인 경우만)
    public List<Map<String, Object>> findTopPlayersByStat(int limit) {
        List<Player> players = playerRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Player player : players) {
            if (player.getStats() == null || player.getStats().isEmpty()) continue;

            for (PlayerStat stat : player.getStats()) {
                Map<String, String> statMap = stat.getStatMap();
                if (statMap == null || statMap.isEmpty()) continue;

                // ✅ "Div." 대신 PlayerStat 또는 Player 자체에 season 컬럼이 있다고 가정
                String season = statMap.getOrDefault("Div.", ""); // 또는 필요에 따라 Player에 season 필드 사용
                if (!"베스트".equals(season)) continue; // 통산 시즌만 골라서

                String pos = statMap.getOrDefault("Pos.", "").toUpperCase();
                double mainStat = 0.0;

                if ("P".equals(pos)) {
                    mainStat = parseDouble(statMap.get("SO")); // 투수는 WAR
                } else {
                    mainStat = parseDouble(statMap.get("비율")); // 타자는 비율
                }

                if (mainStat == 0.0) continue; // mainStat이 0이면 제외

                Map<String, Object> playerInfo = new HashMap<>();
                playerInfo.put("pno", player.getPno());
                playerInfo.put("name", player.getName());
                playerInfo.put("position", pos);
                playerInfo.put("backNumber", player.getBackNumber());
                playerInfo.put("mainStat", mainStat);

                result.add(playerInfo);
            }
        }

        // 결과 출력 (디버깅용)
		/*
		 * System.out.println("===== findTopPlayersByStat 결과 출력 ====="); for
		 * (Map<String, Object> playerInfo : result) { System.out.println(playerInfo); }
		 */

        return result.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("mainStat"), (Double) a.get("mainStat")))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ⬇️ 문자열 -> double 변환
    private double parseDouble(String value) {
        try {
            return value == null ? 0.0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // ⬇️ 문자열 -> int 변환
    private int parseInt(String value) {
        try {
            return value == null ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
