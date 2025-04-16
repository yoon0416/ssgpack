package com.ssgpack.ssgfc;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KboTestCrawler {

    public static void main(String[] args) {
        try {
            crawlTop5PlayerRecords();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void crawlTop5PlayerRecords() throws Exception {
        String url = "https://www.koreabaseball.com/Record/Player/HitterBasic/Basic1.aspx";
        Document doc = Jsoup.connect(url).get();

        Elements links = doc.select("table.tData01 a");
        int count = 0;

        for (Element link : links) {
            if (count >= 5) break;

            String href = link.attr("href");
            if (href.contains("playerId=")) {
                String playerId = href.substring(href.indexOf("=") + 1);
                crawlPlayerRecord(playerId);
                count++;
            }
        }
    }

    public static void crawlPlayerRecord(String playerId) throws Exception {
        String detailUrl = "https://www.koreabaseball.com/Record/Player/HitterDetail/Basic.aspx?playerId=" + playerId;
        Document doc = Jsoup.connect(detailUrl).get();

        String name = doc.selectFirst(".player_basic .player_name").text();
        System.out.println("📌 선수 이름: " + name);

        Element table = doc.selectFirst("table.tData02");
        if (table != null) {
            Elements rows = table.select("tbody > tr");
            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() > 0) {
                    String year = cols.get(0).text();
                    String team = cols.get(1).text();
                    String games = cols.get(2).text();
                    String avg = cols.get(14).text();

                    System.out.printf("  ➤ %s | %s | 경기수: %s | 타율: %s\n", year, team, games, avg);
                }
            }
        } else {
            System.out.println("⚠️ 기록 테이블이 없습니다.");
        }
    }
}

