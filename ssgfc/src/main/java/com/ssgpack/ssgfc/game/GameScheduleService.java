package com.ssgpack.ssgfc.game;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class GameScheduleService {

    private final GameScheduleRepository repository;

    public GameScheduleService(GameScheduleRepository repository) {
        this.repository = repository;
    }

    // 전체 일정 조회 (✅ 날짜 오름차순 정렬)
    public List<GameSchedule> findAllOrderByGameDate() {
        return repository.findAllByOrderByGameDateAsc();
    }

    // 결과로 조회 (예정 / 취소 / 승 / 패 / 무)
    public List<GameSchedule> findByResult(String result) {
        return repository.findByResult(result);
    }

    // 날짜 구간으로 조회
    public List<GameSchedule> findByDateRange(LocalDate start, LocalDate end) {
        return repository.findByGameDateBetween(start, end);
    }
}