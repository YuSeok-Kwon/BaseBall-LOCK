package com.kepg.BaseBallLOCK.team.teamRanking.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.team.teamRanking.domain.TeamRanking;
import com.kepg.BaseBallLOCK.team.teamStats.domain.TeamStats;

public interface TeamRankingRepository extends JpaRepository<TeamRanking, Integer> {
	
    List<TeamRanking> findBySeasonOrderByRankingAsc(int season);
    
    Optional<TeamRanking> findBySeasonAndTeamId(int season, int teamId);
    
    @Query(value = """
            SELECT ts.*
            FROM teamStats ts
            JOIN (
                SELECT category, MAX(value) AS max_value
                FROM teamStats
                WHERE season = :season
                GROUP BY category
            ) max_stats
            ON ts.category = max_stats.category AND ts.value = max_stats.max_value
            WHERE ts.season = :season
        """, nativeQuery = true)
        List<TeamStats> findTopStatsBySeason(@Param("season") int season);

}
