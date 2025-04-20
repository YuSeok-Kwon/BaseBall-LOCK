package com.kepg.BaseBallLOCK.player.stats;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.stats.domain.Stats;

public interface StatsRepository extends JpaRepository<Stats, Integer> {
    List<Stats> findByPlayer(Player player);
    
    List<Stats> findBySeasonAndCategoryOrderByRankingAsc(Integer season, String category);
    
    Optional<Stats> findTopByCategoryAndPlayer_PositionOrderByValueDesc(String category, String position);
    
    @Query(value = "SELECT * FROM stats WHERE playerId = :playerId AND category IN ('WAR', 'AVG', 'OPS', 'HR', 'ERA', 'WHIP', 'W', 'SV', 'HOLD')", nativeQuery = true)
    List<Stats> findKeyStatsByPlayerId(@Param("playerId") int playerId);
}
