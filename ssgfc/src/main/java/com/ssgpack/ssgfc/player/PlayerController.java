// PlayerController.java - 선수 기본 정보 저장 전용 컨트롤러
package com.ssgpack.ssgfc.player;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor

public class PlayerController {

    private final PlayerCrawlingService playerCrawlingService;
    private final PlayerRepository playerRepository;
    
 // ✅ 조회수 메모리 저장소
    private final Map<String, Integer> viewCountMap = new ConcurrentHashMap<>();
    
    @GetMapping("/save-all")
    public String saveAllPlayers() {
        try {
            String teamUrl = "https://statiz.sporki.com/team/?m=seasonBacknumber&t_code=9002&year=2025";
            Connection connection = Jsoup.connect(teamUrl)
                    .userAgent("Mozilla/5.0")
                    .timeout(15000);
            Document doc = connection.get();
            Elements items = doc.select("div.item.away");

            for (Element item : items) {
                Element numberElement = item.selectFirst("span.number");
                Element link = item.selectFirst("a");

                if (numberElement != null && link != null) {
                    String number = numberElement.text().trim();
                    String href = link.attr("href");
                    if (!href.contains("p_no=")) continue;
                    String detailUrl = "https://statiz.sporki.com" + href;

                    try {
                        Thread.sleep((long) (Math.random() * 10000 + 10000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    playerCrawlingService.crawlAndSavePlayerBasic(detailUrl, number);
                }
            }
            return "✅ 선수 기본 정보 저장 완료";
        } catch (IOException e) {
            return "❌ 선수 크롤링 실패: " + e.getMessage();
        }
    }
 // 선수 전체 목록 조회 API
    @GetMapping("/api/players")
    public List<Player> getAllPlayers() {
        return playerCrawlingService.findAllPlayers();
    }

    @GetMapping("/api/players/{pNo}")
    public ResponseEntity<Player> getPlayerDetail(@PathVariable String pNo) {
        Player player = playerRepository.findByPno(pNo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "선수 정보를 찾을 수 없습니다."));

        
        // ✅ 조회수 관리: map에서 꺼내고 1 증가
        int count = viewCountMap.getOrDefault(pNo, 0) + 1;
        viewCountMap.put(pNo, count);

        // ✅ player 객체에 반영해서 클라이언트에 전달
        player.setViewCount(count);
        
        
        return ResponseEntity.ok(player);
    }

    @GetMapping("/api/test")
    public ResponseEntity<?> test() {
        List<Player> all = playerRepository.findAll();
        for (Player p : all) {
            if ("14805".equals(p.getPno())) {
                return ResponseEntity.ok("✔ 직접 찾음: " + p.getName());
            }
        }
        return ResponseEntity.status(404).body("❌ 직접 찾아도 없음");
    }
}
