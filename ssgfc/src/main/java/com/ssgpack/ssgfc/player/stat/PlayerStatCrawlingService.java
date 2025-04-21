package com.ssgpack.ssgfc.player.stat;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.ssgpack.ssgfc.player.Player;
import com.ssgpack.ssgfc.player.PlayerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerStatCrawlingService {

    private final PlayerStatRepository playerStatRepository;
    private final PlayerRepository playerRepository;

    public void crawlAndSavePlayerStat(String url, Player player) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();

        // 주요 기록 테이블 찾기
        Element table = doc.selectFirst("div.box_cont table");
        if (table == null) {
            System.out.println("❌ 주요 기록 테이블을 찾을 수 없습니다: " + url);
            return;
        }

        // DB에 저장된 Player 찾기 (pno 기준)
        Optional<Player> optionalPlayer = playerRepository.findTopByPno(player.getPno());
        if (optionalPlayer.isEmpty()) {
            System.out.println("❌ DB에서 선수를 찾을 수 없습니다: " + player.getPno());
            return;
        }
        Player savedPlayer = optionalPlayer.get();

        Elements rows = table.select("tbody > tr");
        for (Element row : rows) {
            Elements tds = row.select("td");
            if (tds.size() >= 34) {
                String season = tds.get(1).text(); // 시즌 연도
                Double avg = parseDouble(tds.get(27).text());
                Double obp = parseDouble(tds.get(28).text());
                Double slg = parseDouble(tds.get(29).text());
                Double war = parseDouble(tds.get(33).text());

                // 중복 기록 방지 (해당 시즌 기록이 이미 저장되어 있는지 확인)
                Optional<PlayerStat> existing = playerStatRepository.findByPlayerAndSeason(savedPlayer, season);
                if (existing.isPresent()) {
                    System.out.println("✔ 이미 저장된 기록: " + savedPlayer.getName() + " (" + season + ")");
                    continue;
                }

                // 새 기록 저장
                PlayerStat stat = PlayerStat.builder()
                        .season(season)
                        .avg(avg)
                        .obp(obp)
                        .slg(slg)
                        .war(war)
                        .player(savedPlayer)
                        .build();

                playerStatRepository.save(stat);
                System.out.println("📊 주요 기록 저장 완료: " + savedPlayer.getName() + " (" + season + ")");
            }
        }
    }

    private Double parseDouble(String text) {
        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            return null;
        }
    }
    public List<PlayerStat> findAllStats() {
        return playerStatRepository.findAll();
    }
}
