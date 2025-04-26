package com.kepg.BaseBallLOCK.player.stats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.player.stats.domain.PitcherStats;

public interface PitcherStatsRepository extends JpaRepository<PitcherStats, Integer> {
	
    Optional<PitcherStats> findByPlayerIdAndSeasonAndCategory(Integer playerId, Integer season, String category);
    
    @Query("SELECT s.value FROM PitcherStats s WHERE s.playerId = :playerId AND s.category = :category AND s.season = :season")
    Optional<String> findStatValueByPlayerIdCategoryAndSeason(@Param("playerId") int playerId, @Param("category") String category, @Param("season") int season);
}
