package com.ssgpack.ssgfc.service;

import com.ssgpack.ssgfc.entity.GameSchedule;
import com.ssgpack.ssgfc.repository.GameScheduleRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class ScheduleCrawlerService {

    private final GameScheduleRepository repository;

    public ScheduleCrawlerService(GameScheduleRepository repository) {
        this.repository = repository;
    }

    public void crawlAndSave() {
        try {
            String url = "https://statiz.sporki.com/schedule/?year=2025&month=4";
            Document doc = Jsoup.connect(url)
                                .timeout(10000)
                                .userAgent("Mozilla/5.0")
                                .get();

            Elements gameCells = doc.select("div.calendar_area td");

            LocalDate today = LocalDate.now();
            int year = 2025;
            int month = 4;

            for (Element cell : gameCells) {
                String dayStr = cell.select("span.day").text();
                if (dayStr == null || dayStr.isEmpty()) continue;

                int dayInt = Integer.parseInt(dayStr);
                LocalDate gameDate = LocalDate.of(year, month, dayInt);

                // 월요일 제외
                if (gameDate.getDayOfWeek() == DayOfWeek.MONDAY) continue;

                Elements games = cell.select("div.games li");

                for (Element game : games) {
                    Elements teams = game.select("span.team");
                    Elements scores = game.select("span.score");

                    if (teams.size() == 2) {
                        String team1 = teams.get(0).text();
                        String team2 = teams.get(1).text();

                        if (team1.contains("SSG") || team2.contains("SSG")) {

                            String score1 = scores.size() >= 2 ? scores.get(0).text() : null;
                            String score2 = scores.size() >= 2 ? scores.get(1).text() : null;

                            String status;
                            if (gameDate.isBefore(today)) {
                                status = (score1 != null && score2 != null) ? "경기 결과" : "경기 취소";
                            } else {
                                status = "경기 예정";
                            }

                            GameSchedule schedule = new GameSchedule(
                                    gameDate, team1, team2, score1, score2, status
                            );
                            repository.save(schedule);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}