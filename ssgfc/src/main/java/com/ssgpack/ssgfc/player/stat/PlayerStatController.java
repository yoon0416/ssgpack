
// PlayerStatController.java - 선수 주요 기록 저장 전용 컨트롤러
package com.ssgpack.ssgfc.player.stat;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssgpack.ssgfc.player.Player;
import com.ssgpack.ssgfc.player.PlayerRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PlayerStatController {

    private final PlayerRepository playerRepository;
    private final PlayerStatCrawlingService playerStatCrawlingService;

    

    
    @GetMapping("/player-stats/test-one")
    public String testOnePlayer() {
        try {
            // 테스트할 선수 정보
            Player player = Player.builder()
                    .name("최정")
                    .pno("10106") // ⭐ p_no 직접 지정
                    .build();

            String url = "https://statiz.sporki.com/player/?m=playerinfo&p_no=" + player.getPno();

            playerStatCrawlingService.crawlAndSavePlayerStat(url, player);

            return "✅ 주요 기록 크롤링 완료: " + player.getName();
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ 오류 발생: " + e.getMessage();
        }
    }

    
    
    
    
    
    
    
    
    
    @GetMapping("/player-stats/save-all")
    public String saveAllStats() {
        List<Player> players = playerRepository.findAll();

        for (Player player : players) {
            if (player.getPno() == null || player.getPno().isEmpty()) {
                System.out.println("⚠️ pno 없음, 건너뜀: " + player.getName());
                continue;
            }

            try {
                String url = "https://statiz.sporki.com/player/?m=playerinfo&p_no=" + player.getPno();
                System.out.println("➡️ 저장 시도: " + player.getName() + " (" + player.getPno() + ")");
                playerStatCrawlingService.crawlAndSavePlayerStat(url, player);
                Thread.sleep((long) (Math.random() * 5000 + 5000));
            } catch (Exception e) {
                System.out.println("❌ 기록 저장 실패: " + player.getName() + " (" + player.getPno() + ")");
                e.printStackTrace();
            }
        }

        return "✅ 전체 주요 기록 저장 완료!";
    }



}