package com.ssgpack.ssgfc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssgpack.ssgfc.entity.GameSchedule;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, Long> {

}
