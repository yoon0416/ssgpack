package com.ssgpack.ssgfc.game;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Location findByShortName(String shortName);    // 예: 문학, 잠실
    Location findByTeamKey(String teamKey);        // 예: sk, doosan
    Location findByStadiumName(String stadiumName);
    
}