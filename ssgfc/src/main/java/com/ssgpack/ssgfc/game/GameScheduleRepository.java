package com.ssgpack.ssgfc.game;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {
	List<GameSchedule> findByResult(String result);
	List<GameSchedule> findByGameDateBetween(LocalDate start, LocalDate end);
	List<GameSchedule> findByGameDate(LocalDate gameDate);
	List<GameSchedule> findByGameDateAfter(LocalDate gameDate);
	List<GameSchedule> findByLocationIsNullOrLocationEquals(String empty);
	List<GameSchedule> findByLocationOrderByGameDateAsc(String location);
	List<GameSchedule> findAllByOrderByGameDateAsc();
}
