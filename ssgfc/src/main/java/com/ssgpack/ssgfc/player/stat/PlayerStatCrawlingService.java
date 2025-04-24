package com.ssgpack.ssgfc.player.stat;

import com.ssgpack.ssgfc.player.Player;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlayerStatCrawlingService {

    private final PlayerStatRepository playerStatRepository;

    public void crawlAndSavePlayerStat(String url, Player player) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();

        // 주요 기록 테이블 찾기
        Element statTable = doc.select("div.table_type03.transverse_scroll table").first();
        if (statTable == null) {
            System.out.println("⚠️ 기록 테이블 없음: " + player.getName());
            return;
        }

        // 헤더 추출
        List<String> headers = new ArrayList<>();
        Elements headerRows = statTable.select("thead tr");
        for (Element headerRow : headerRows) {
            for (Element th : headerRow.select("th")) {
                headers.add(th.text().trim());
            }
        }

        // 데이터 행 추출
        Elements dataRows = statTable.select("tbody tr");
        for (Element row : dataRows) {
            Elements cols = row.select("td, th");
            if (cols.isEmpty()) continue;

            String season = cols.get(0).text().trim();

            // ✅ 중복 체크
            boolean exists = playerStatRepository.existsByPlayerAndSeason(player, season);
            if (exists) {
                System.out.println("⏩ 이미 저장됨 (스킵): " + player.getName() + " / " + season);
                continue;
            }

            Map<String, String> statMap = new LinkedHashMap<>();
            for (int i = 0; i < Math.min(headers.size(), cols.size()); i++) {
                String key = headers.get(i);
                String value = cols.get(i).text().trim();
                statMap.put(key, value);
            }

            PlayerStat playerStat = PlayerStat.builder()
                    .season(season)
                    .statMap(statMap)
                    .player(player)
                    .build();

            playerStatRepository.save(playerStat);
            System.out.println("✅ 저장 완료: " + player.getName() + " / " + season);
        }
    }
}
