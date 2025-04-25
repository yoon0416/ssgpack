package com.ssgpack.ssgfc.game;

import java.time.LocalDate;
import java.util.List;

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

    // 전체 경기 일정 조회
    @GetMapping
    public List<GameSchedule> all() {
        return service.findAll();
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
    
}