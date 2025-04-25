package com.ssgpack.ssgfc.game;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class SSGScheduleCrawler {

    public static void main(String[] args) {
        try {
            String url = "https://statiz.sporki.com/schedule/?year=2025&month=4";
            
            // ✅ 브라우저처럼 위장
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0.0.0 Safari/537.36")
                    .referrer("https://www.google.com/")
                    .timeout(15000)
                    .get();

            Elements gameCells = doc.select("div.calendar_area td");

            LocalDate today = LocalDate.now();
            int year = 2025;
            int month = 4;

            for (Element cell : gameCells) {

                // ✅ 요청 간 딜레이 (1.5초)
                Thread.sleep(1500);

                String dayStr = cell.select("span.day").text();
                if (dayStr == null || dayStr.isEmpty()) continue;

                int dayInt = Integer.parseInt(dayStr);
                LocalDate gameDate = LocalDate.of(year, month, dayInt);

                // 월요일 스킵
                if (gameDate.getDayOfWeek() == DayOfWeek.MONDAY) continue;

                Elements games = cell.select("div.games li");

                for (Element game : games) {
                    Elements teams = game.select("span.team");
                    Elements scores = game.select("span.score");

                    if (teams.size() == 2) {
                        String team1 = teams.get(0).text();
                        String team2 = teams.get(1).text();

                        String score1 = scores.size() >= 2 ? scores.get(0).text() : "";
                        String score2 = scores.size() >= 2 ? scores.get(1).text() : "";

                        if (team1.contains("SSG") || team2.contains("SSG")) {
                            // 오늘 이전
                            if (gameDate.isBefore(today)) {
                                if (!score1.isEmpty() && !score2.isEmpty()) {
                                    System.out.println("4월 " + dayStr + "일 | " + team1 + " " + score1 + " vs " + team2 + " " + score2);
                                } else {
                                    System.out.println("4월 " + dayStr + "일 | 경기 취소");
                                }
                            } else {
                                System.out.println("4월 " + dayStr + "일 | 경기 예정");
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠ 크롤링 중 오류 발생");
            e.printStackTrace();
        }
    }
}