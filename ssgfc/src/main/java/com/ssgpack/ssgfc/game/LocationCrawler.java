package com.ssgpack.ssgfc.game;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@Component
@RequiredArgsConstructor
public class LocationCrawler {

    private final GameScheduleRepository gameScheduleRepository;

    public void crawlAndUpdateLocations() {
        int total = 0;
        int success = 0;
        int skip = 0;
        int fail = 0;

        try {
            LocalDate startDate = LocalDate.of(2025, 4, 1);
            LocalDate endDate = LocalDate.of(2025, 5, 31);

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                try {
                    String url = "https://m.sports.naver.com/kbaseball/schedule/index?date=" + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0")
                            .timeout(10000)
                            .get();

                    Elements gameElements = doc.select(".sch_tb tbody tr");

                    for (Element game : gameElements) {
                        try {
                            Element teamElement = game.selectFirst(".team");
                            Element placeElement = game.selectFirst(".place");

                            if (teamElement == null || placeElement == null) {
                                continue;
                            }

                            String teams = teamElement.text().trim(); // 팀명들
                            String location = placeElement.text().trim(); // 구장명

                            if (!teams.contains("SSG")) {
                                continue;
                            }

                            GameSchedule schedule = gameScheduleRepository.findByGameDate(date)
                                    .stream()
                                    .findFirst()
                                    .orElse(null);

                            if (schedule != null) {
                                if (schedule.getLocation() == null || schedule.getLocation().isBlank()) {
                                    schedule.setLocation(location);
                                    gameScheduleRepository.save(schedule);
                                    System.out.println("✅ 저장 완료: " + date + " / " + location);
                                    success++;
                                } else {
                                    System.out.println("ℹ️ 이미 등록된 경기: " + date + " / " + schedule.getLocation());
                                    skip++;
                                }
                            } else {
                                System.out.println("⚠ DB에 해당 날짜 없음: " + date);
                                fail++;
                            }
                        } catch (Exception inner) {
                            System.err.println("❌ 경기 정보 처리 실패: " + inner.getMessage());
                            fail++;
                        }
                    }
                    total++;
                } catch (Exception e) {
                    System.err.println("❌ [" + date + "] 페이지 크롤링 실패: " + e.getMessage());
                    fail++;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 전체 크롤링 실패: " + e.getMessage());
        }

        System.out.println("\n----- ✅ 크롤링 요약 -----");
        System.out.println("총 시도: " + total);
        System.out.println("성공: " + success);
        System.out.println("스킵: " + skip);
        System.out.println("실패: " + fail);
        System.out.println("--------------------------");
    }
}
