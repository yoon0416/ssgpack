package com.ssgpack.ssgfc.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssgpack.ssgfc.entity.GameSchedule;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {
	List<GameSchedule> findByResult(String result);
	List<GameSchedule> findByGameDateBetween(LocalDate start, LocalDate end);
	
}
