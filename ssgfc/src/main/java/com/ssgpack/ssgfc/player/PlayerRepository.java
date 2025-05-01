package com.ssgpack.ssgfc.player;

import java.util.List;
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

	  // ✅ 여기에 fetch join 추가!
	    @Query("SELECT p FROM Player p LEFT JOIN FETCH p.stats WHERE p.pno = :pno")
	    Optional<Player> findByPnoWithStats(@Param("pno") String pno);
	    
	    // ✅ 여기에 추가!
	    @Query("SELECT DISTINCT p FROM Player p LEFT JOIN FETCH p.stats")
	    List<Player> findAllWithStats();
	    
}

