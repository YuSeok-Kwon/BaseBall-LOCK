package com.kepg.BaseBallLOCK.player.stats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.player.stats.domain.PitcherStats;

public interface PitcherStatsRepository extends JpaRepository<PitcherStats, Integer> {
	
    Optional<PitcherStats> findByPlayerIdAndSeasonAndCategory(Integer playerId, Integer season, String category);
    
    @Query("""
    	    SELECT p FROM PitcherStats p
    	    WHERE p.playerId = :playerId
    	      AND p.category IN ('ERA', 'WHIP', 'W', 'SV', 'HOLD')
    	""")
    	List<PitcherStats> findKeyStatsByPlayerId(@Param("playerId") int playerId);
}
