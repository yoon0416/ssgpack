package com.ssgpack.ssgfc.player;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
	  Optional<Player> findTopByPno(String pno);
	  @Query("SELECT p FROM Player p WHERE p.pno = :pno")
	  Optional<Player> findByPno(@Param("pno") String pno);
}
