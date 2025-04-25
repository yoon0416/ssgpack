package com.ssgpack.ssgfc.review;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SSGReviewURLCrawler {

    public static void main(String[] args) {
        List<String> gameUrls = new ArrayList<>();

        LocalDate today = LocalDate.now();
        int[] targetMonths = {
                today.minusMonths(1).getMonthValue(),
                today.getMonthValue(),
                today.plusMonths(1).getMonthValue()
        };
        int year = today.getYear();

        for (int month : targetMonths) {
            try {
                String url = "https://m.sports.naver.com/kbaseball/schedule/index?date=" + year + "-" + String.format("%02d", month) + "-01";
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .referrer("https://m.sports.naver.com")
                        .get();

                Elements links = doc.select("a[href*=/game/]");

                for (Element link : links) {
                    String href = link.attr("href");
                    if (href.contains("SSG")) {
                        // URL 예시: /game/20250420LGSK02025/preview
                        String[] parts = href.split("/");
                        for (String part : parts) {
                            if (part.matches("\\d{8}[A-Z]{4}\\d{5}")) {
                                gameUrls.add(part);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("❌ 오류 발생 (월: " + month + "): " + e.getMessage());
            }
        }

        System.out.println("✅ 추출된 SSG 관련 gameUrl 목록:");
        for (String url : gameUrls) {
            System.out.println(url);
        }
    }
}
