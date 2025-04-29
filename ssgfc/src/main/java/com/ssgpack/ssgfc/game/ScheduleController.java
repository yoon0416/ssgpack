package com.ssgpack.ssgfc.game;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class ScheduleController {

    private final GameScheduleService service;
    private final GameScheduleRepository gameScheduleRepository;

    // 전체 경기 일정 조회
    @GetMapping
    public List<GameSchedule> all() {
    	return service.findAllOrderByGameDate();
    }

    // 결과(예정, 취소, 승, 패 등)별 경기 조회
    @GetMapping("/result/{result}")
    public List<GameSchedule> byResult(@PathVariable String result) {
        return service.findByResult(result);
    }

    // 날짜 구간으로 경기 조회
    @GetMapping("/range")
    public List<GameSchedule> byDateRange(@RequestParam String start,
                                          @RequestParam String end) {
        LocalDate s = LocalDate.parse(start);
        LocalDate e = LocalDate.parse(end);
        return service.findByDateRange(s, e);
    }
    
    @GetMapping("/api/month-record")
    public ResponseEntity<Map<String, Integer>> getMonthRecord(
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Integer month
    ) {
        LocalDate now = LocalDate.now();
        // year나 month가 없으면 현재 날짜 기준
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();

        LocalDate startOfMonth = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        List<GameSchedule> games = gameScheduleRepository.findByGameDateBetween(startOfMonth, endOfMonth);

        int wins = 0;
        int losses = 0;
        int draws = 0;

        for (GameSchedule game : games) {
            if (game.getScore1() == null || game.getScore2() == null) {
                continue; // 점수 없는 경기는 패스
            }

            boolean ssgIsFirst = "SSG".equals(game.getTeam1());

            int ssgScore = ssgIsFirst ? game.getScore1() : game.getScore2();
            int oppScore = ssgIsFirst ? game.getScore2() : game.getScore1();

            if (ssgScore > oppScore) {
                wins++;
            } else if (ssgScore < oppScore) {
                losses++;
            } else {
                draws++;
            }
        }

        Map<String, Integer> record = new HashMap<>();
        record.put("wins", wins);
        record.put("losses", losses);
        record.put("draws", draws);

        return ResponseEntity.ok(record);
    }
}