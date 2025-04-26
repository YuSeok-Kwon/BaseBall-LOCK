package com.kepg.BaseBallLOCK.team.teamStats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.team.teamStats.domain.TeamStats;
import com.kepg.BaseBallLOCK.team.teamStats.dto.TopStatTeamInterface;

public interface TeamStatsRepository extends JpaRepository<TeamStats, Integer> {
    Optional<TeamStats> findByTeamIdAndSeasonAndCategory(Integer teamId, Integer season, String category);
    
    Optional<TeamStats> findByTeamIdAndSeasonAndCategory(int teamId, int season, String category);
    
    @Query(value = """
    	    SELECT 
    	        ts.category AS category,
    	        ts.value AS value,
    	        t.id AS teamId,
    	        t.name AS teamName,
    	        t.logoName AS teamLogo
    	    FROM teamStats ts
    	    JOIN team t ON ts.teamId = t.id
    	    WHERE ts.season = :season AND ts.category = :category
    	    ORDER BY ts.value DESC
    	    LIMIT 1
    	""", nativeQuery = true)
    	TopStatTeamInterface findTopByCategoryAndSeasonMax(@Param("season") int season, @Param("category") String category);

    	@Query(value = """
    	    SELECT 
    	        ts.category AS category,
    	        ts.value AS value,
    	        t.id AS teamId,
    	        t.name AS teamName,
    	        t.logoName AS teamLogo
    	    FROM teamStats ts
    	    JOIN team t ON ts.teamId = t.id
    	    WHERE ts.season = :season AND ts.category = :category
    	    ORDER BY ts.value ASC
    	    LIMIT 1
    	""", nativeQuery = true)
    	TopStatTeamInterface findTopByCategoryAndSeasonMin(@Param("season") int season, @Param("category") String category);
}
