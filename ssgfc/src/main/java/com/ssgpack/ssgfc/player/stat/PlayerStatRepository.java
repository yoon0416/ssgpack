package com.ssgpack.ssgfc.player.stat;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ssgpack.ssgfc.player.Player;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {
    // 필요한 경우 아래처럼 커스텀 메서드도 추가 가능
    // List<PlayerStat> findByPlayerId(Long playerId);
	
	Optional<PlayerStat> findByPlayerAndSeason(Player player, String season);
	boolean existsByPlayerAndSeason(Player player, String season);

}
