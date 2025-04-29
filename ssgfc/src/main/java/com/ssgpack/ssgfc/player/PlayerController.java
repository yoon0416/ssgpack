package com.ssgpack.ssgfc.player;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerCrawlingService playerCrawlingService;
    private final PlayerRepository playerRepository;
    private final PlayerService playerService;

    private final Map<String, Integer> viewCountMap = new ConcurrentHashMap<>();

    @GetMapping("/api/players")
    public List<Player> getAllPlayers() {
        return playerCrawlingService.findAllPlayers();
    }

    @GetMapping("/api/players/{pNo}")
    public ResponseEntity<Player> getPlayerDetail(@PathVariable String pNo) {
        Player player = playerRepository.findByPno(pNo)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "선수 정보를 찾을 수 없습니다."));

        int count = viewCountMap.getOrDefault(pNo, 0) + 1;
        viewCountMap.put(pNo, count);
        player.setViewCount(count);

        return ResponseEntity.ok(player);
    }

    @GetMapping("/api/players/top-hit")
    public List<Player> getTopHitPlayers(@RequestParam(defaultValue = "5") int limit) {
        return playerRepository.findAll().stream()
            .peek(player -> {
                int viewCount = viewCountMap.getOrDefault(player.getPno(), 0);
                player.setViewCount(viewCount);
            })
            .sorted((p1, p2) -> Integer.compare(p2.getViewCount(), p1.getViewCount()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    @GetMapping("/api/players/top-stat")
    public List<Map<String, Object>> getTopPlayersByStat(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return playerService.findTopPlayersByStat(limit);
    }

    @GetMapping("/api/players/top-hitters")
    public List<Map<String, Object>> getTopHitters(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return playerService.findTopPlayersByStat(100).stream()  // 100명 충분히 가져오기
                .filter(player -> !"P".equals(player.get("position"))) // 타자만
                .sorted((p1, p2) -> Double.compare(
                    Double.parseDouble(p2.getOrDefault("mainStat", "0").toString()),
                    Double.parseDouble(p1.getOrDefault("mainStat", "0").toString())
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/players/top-pitchers")
    public List<Map<String, Object>> getTopPitchers(
            @RequestParam(defaultValue = "3") int limit
    ) {
        return playerService.findTopPlayersByStat(100).stream()  // 100명 충분히 가져오기
                .filter(player -> "P".equals(player.get("position"))) // 투수만
                .sorted((p1, p2) -> Double.compare(
                    Double.parseDouble(p2.getOrDefault("mainStat", "0").toString()),
                    Double.parseDouble(p1.getOrDefault("mainStat", "0").toString())
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }

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

    @GetMapping("/api/test")
    public ResponseEntity<?> test() {
        return playerRepository.findAll().stream()
                .filter(p -> "14805".equals(p.getPno()))
                .findFirst()
                .map(p -> ResponseEntity.ok("✔ 직접 찾음: " + p.getName()))
                .orElse(ResponseEntity.status(404).body("❌ 직접 찾아도 없음"));
    }
}
