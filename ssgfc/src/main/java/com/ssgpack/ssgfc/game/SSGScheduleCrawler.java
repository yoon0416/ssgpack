package com.ssgpack.ssgfc.game;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SSGScheduleCrawler {

    private final GameScheduleRepository gameScheduleRepository;

    public void crawlAndSave() {
        int year = 2025; // 필요하면 나중에 LocalDate.now().getYear()로 바꿀 수 있음

        for (int month = 4; month <= 5; month++) {
            String url = "https://statiz.sporki.com/schedule/?year=" + year + "&month=" + month;

            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123.0.0.0 Safari/537.36")
                        .referrer("https://www.google.com/")
                        .timeout(15000)
                        .get();

                Elements gameCells = doc.select("div.calendar_area td"); // html td뽑기

                for (Element cell : gameCells) {
                    Thread.sleep(500);

                    String dayStr = cell.select("span.day").text();
                    if (dayStr.isEmpty()) continue;

                    int dayInt = Integer.parseInt(dayStr);
                    LocalDate gameDate = LocalDate.of(year, month, dayInt);

                    Elements games = cell.select("div.games li");

                    if (games.isEmpty()) continue;

                    for (Element game : games) {
                        Elements teams = game.select("span.team");
                        Elements scores = game.select("span.score");
                        Element hourElement = game.selectFirst("span.hour");

                        String startTimeStr = (hourElement != null) ? hourElement.text() : "18:30";

                        if (teams.size() == 2) {
                            String team1 = teams.get(0).text();
                            String team2 = teams.get(1).text();

                            String score1 = scores.size() >= 2 ? scores.get(0).text() : "";
                            String score2 = scores.size() >= 2 ? scores.get(1).text() : "";
                            

                            if (team1.contains("SSG") || team2.contains("SSG")) {
                                // ✅ 오늘 날짜에 해당하는 경기 찾기
                                GameSchedule schedule = gameScheduleRepository.findByGameDate(gameDate)
                                        .stream().findFirst().orElse(null);

                                if (schedule != null) {
                                    // ✅ 기존 경기 있으면 업데이트
                                    if (!score1.isEmpty() && !score2.isEmpty()) {
                                        schedule.setResult(team1 + " " + score1 + " : " + team2 + " " + score2);
                                        schedule.setScore1(Integer.parseInt(score1));
                                        schedule.setScore2(Integer.parseInt(score2));
                                    }
                                    if (startTimeStr != null && (schedule.getStartTime() == null || schedule.getStartTime().isEmpty())) {
                                        schedule.setStartTime(startTimeStr);
                                    }
                                    gameScheduleRepository.save(schedule);
                                    System.out.println("✅ 업데이트 완료: " + gameDate);
                                } else {
                                    // ✅ 없으면 새로 저장
                                    GameSchedule newGame = new GameSchedule(
                                            gameDate,
                                            null,
                                            (score1.isEmpty() || score2.isEmpty()) ? "경기 예정" : (team1 + " " + score1 + " : " + team2 + " " + score2),
                                            null,
                                            team1,
                                            team2,
                                            score1.isEmpty() ? null : Integer.parseInt(score1),
                                            score2.isEmpty() ? null : Integer.parseInt(score2)
                                    );
                                    if (startTimeStr != null) {
                                        newGame.setStartTime(startTimeStr);
                                    }
                                    gameScheduleRepository.save(newGame);
                                    System.out.println("✅ 새로 추가 완료: " + gameDate);
                                }
                            }
                        }
                    }
                }

            } catch (Exception e) {
                System.err.println("⚠ 크롤링 오류: " + e.getMessage());
            }
        }
    }
}
