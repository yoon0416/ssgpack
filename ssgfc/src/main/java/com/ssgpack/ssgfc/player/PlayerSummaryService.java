package com.ssgpack.ssgfc.player;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.ssgpack.ssgfc.player.stat.PlayerStat;
import com.ssgpack.ssgfc.player.stat.PlayerStatRepository;
import com.ssgpack.ssgfc.review.OpenAIUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerSummaryService {

    private final PlayerRepository playerRepository;
    private final PlayerStatRepository playerStatRepository;
    private final OpenAIUtil openAIUtil;

    public void generatePlayerSummaries() {
        List<Player> players = playerRepository.findAll();

        for (Player player : players) {
            if (player.getSummary() != null && !player.getSummary().isEmpty()) continue;

            List<PlayerStat> stats = playerStatRepository.findAll(); // 또는 player별 최신 시즌 1개만 필터링
            PlayerStat recentStat = stats.stream()
                .filter(stat -> stat.getPlayer().equals(player))
                .max(Comparator.comparing(PlayerStat::getSeason)) // 최신 시즌
                .orElse(null);

            if (recentStat == null) continue;

            Map<String, String> statMap = recentStat.getStatMap();
            StringBuilder statInfo = new StringBuilder();
            statMap.forEach((k, v) -> statInfo.append(k).append(": ").append(v).append(", "));

            String prompt = String.format(
                "이 선수는 포지션: %s, 이름: %s이며 주요 스탯은 다음과 같습니다:\n%s\n" +
                "이 정보를 바탕으로 팬 입장에서 감정 없이 50자 이내로 한줄평을 작성하세요.",
                player.getPosition(), player.getName(), statInfo.toString()
            );

            try {
                String summary = openAIUtil.getSummary(prompt);
                player.setSummary(summary);
                playerRepository.save(player);
                System.out.println("✅ 저장 완료: " + player.getName());
            } catch (Exception e) {
                System.out.println("❌ 실패: " + player.getName());
            }
        }
    }
}