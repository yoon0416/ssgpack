package com.ssgpack.ssgfc.player;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class KboCrawler implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        String url = "https://www.koreabaseball.com/Record/Player/HitterBasic/Basic1.aspx?teamCode=SSG";

        // Jsoup으로 HTML 요청
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .get();

        // 기록 테이블 찾기
        Element table = doc.selectFirst("div.record_result table.tData");

        if (table != null) {
            Elements rows = table.select("tbody > tr");

            for (Element row : rows) {
                Elements cols = row.select("td");

                if (cols.size() > 0) {
                    String name = cols.get(1).text(); // 선수명
                    String team = cols.get(2).text(); // 팀명
                    String avg = cols.get(7).text();  // 타율 (AVG)

                    System.out.println(name + " | " + team + " | AVG: " + avg);
                }
            }
        } else {
            System.out.println("⚠️ 기록 테이블을 찾지 못했습니다.");
        }
    }
}
