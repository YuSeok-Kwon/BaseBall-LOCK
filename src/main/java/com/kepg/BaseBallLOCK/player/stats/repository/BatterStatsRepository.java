package com.kepg.BaseBallLOCK.player.stats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.player.stats.domain.BatterStats;

public interface BatterStatsRepository extends JpaRepository<BatterStats, Integer> {

    Optional<BatterStats> findByPlayerIdAndSeasonAndCategory(Integer playerId, Integer season, String category);
    
    @Query("""
    	    SELECT b FROM BatterStats b
    	    WHERE b.playerId = :playerId
    	      AND b.category IN ('AVG', 'HR', 'OPS')
    	""")
    	List<BatterStats> findKeyStatsByPlayerId(@Param("playerId") int playerId);
}
