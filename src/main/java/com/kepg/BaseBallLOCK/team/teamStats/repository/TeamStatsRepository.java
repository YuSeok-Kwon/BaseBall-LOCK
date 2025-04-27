package com.kepg.BaseBallLOCK.team.teamStats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.team.teamStat.dto.TeamStatRankingInterface;
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
    	
    	@Query(value = """
    		    SELECT 
    		      t.id AS teamId,
    		      t.name AS teamName,
    		      t.logoName AS logoName,
    		      MAX(CASE WHEN ts.category = 'OPS' THEN ts.value END) AS ops,
    		      MAX(CASE WHEN ts.category = 'AVG' THEN ts.value END) AS avg,
    		      MAX(CASE WHEN ts.category = 'HR' THEN ts.value END) AS hr,
    		      MAX(CASE WHEN ts.category = 'SB' THEN ts.value END) AS sb,
    		      MAX(CASE WHEN ts.category = 'BetterWAR' THEN ts.value END) AS betterWar,
    		      MAX(CASE WHEN ts.category = 'PitcherWAR' THEN ts.value END) AS pitcherWar,
    		      MAX(CASE WHEN ts.category = 'SO' THEN ts.value END) AS so,
    		      MAX(CASE WHEN ts.category = 'W' THEN ts.value END) AS w,
    		      MAX(CASE WHEN ts.category = 'H' THEN ts.value END) AS h,
    		      MAX(CASE WHEN ts.category = 'SV' THEN ts.value END) AS sv,
    		      MAX(CASE WHEN ts.category = 'ERA' THEN ts.value END) AS era,
    		      MAX(CASE WHEN ts.category = 'WHIP' THEN ts.value END) AS whip,
    		      MAX(CASE WHEN ts.category = 'BB' THEN ts.value END) AS bb,
    		      MAX(CASE WHEN ts.category = '타격' THEN ts.value END) AS battingWaa,
    		      MAX(CASE WHEN ts.category = '주루' THEN ts.value END) AS baserunningWaa,
    		      MAX(CASE WHEN ts.category = '수비' THEN ts.value END) AS defenseWaa,
    		      MAX(CASE WHEN ts.category = '선발' THEN ts.value END) AS startingWaa,
    		      MAX(CASE WHEN ts.category = '불펜' THEN ts.value END) AS bullpenWaa
    		    FROM teamStats ts
    		    JOIN team t ON ts.teamId = t.id
    		    WHERE ts.season = :season
    		    GROUP BY t.id, t.name, t.logoName
    		""", nativeQuery = true)
    		List<TeamStatRankingInterface> findAllTeamStats(@Param("season") int season);
}
