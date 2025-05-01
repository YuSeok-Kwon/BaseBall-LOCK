package com.kepg.BaseBallLOCK.player.stats.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.player.stats.domain.BatterStats;

public interface BatterStatsRepository extends JpaRepository<BatterStats, Integer> {

    Optional<BatterStats> findByPlayerIdAndSeasonAndCategory(Integer playerId, Integer season, String category);
    
    @Query("SELECT s.value FROM BatterStats s WHERE s.playerId = :playerId AND s.category = :category AND s.season = :season")
    Optional<String> findStatValueByPlayerIdCategoryAndSeason(@Param("playerId") int playerId, @Param("category") String category, @Param("season") int season);

    // 포지션별 WAR 1위 (타자 Top)
    @Query(value = """
    	    WITH ranked_players AS (
    	        SELECT 
    	            bs.position AS position,
    	            p.name AS playerName,
    	            t.name AS teamName,
    	            t.logoName AS logoName,
    	            COALESCE(bs.value, 0) AS war,
    	            ROW_NUMBER() OVER (PARTITION BY bs.position ORDER BY COALESCE(bs.value, 0) DESC) AS row_num
    	        FROM BatterStats bs
    	        JOIN Player p ON bs.playerId = p.id
    	        JOIN Team t ON p.teamId = t.id
    	        WHERE bs.season = :season
    	          AND bs.category = 'WAR'
    	    )
    	    SELECT 
    	        position,
    	        playerName,
    	        teamName,
    	        logoName,
    	        war
    	    FROM ranked_players
    	    WHERE row_num = 1
    	    ORDER BY position
    	    """, nativeQuery = true)
    List<Object[]> findTopBattersByPosition(@Param("season") int season);

    @Query("""
    	    SELECT 
    	        b.position AS position,
    	        p.name AS playerName,
    	        t.name AS teamName,
    	        t.logoName AS logoName,
    	        MAX(CASE WHEN b.category = 'WAR' THEN b.value ELSE NULL END) AS war,
    	        MAX(CASE WHEN b.category = 'AVG' THEN b.value ELSE NULL END) AS avg,
    	        MAX(CASE WHEN b.category = 'OPS' THEN b.value ELSE NULL END) AS ops,
    	        MAX(CASE WHEN b.category = 'HR' THEN b.value ELSE NULL END) AS hr,
    	        MAX(CASE WHEN b.category = 'SB' THEN b.value ELSE NULL END) AS sb,
    	        MAX(CASE WHEN b.category = 'wRC+' THEN b.value ELSE NULL END) AS wrcPlus,
    	        MAX(CASE WHEN b.category = 'G' THEN b.value ELSE NULL END) AS g,
    	        MAX(CASE WHEN b.category = 'PA' THEN b.value ELSE NULL END) AS pa,
    	        MAX(CASE WHEN b.category = 'H' THEN b.value ELSE NULL END) AS h,
    	        MAX(CASE WHEN b.category = 'RBI' THEN b.value ELSE NULL END) AS rbi,
    	        MAX(CASE WHEN b.category = 'BB' THEN b.value ELSE NULL END) AS bb,
    	        MAX(CASE WHEN b.category = 'SO' THEN b.value ELSE NULL END) AS so,
    	        MAX(CASE WHEN b.category = '2B' THEN b.value ELSE NULL END) AS twoB,
    	        MAX(CASE WHEN b.category = '3B' THEN b.value ELSE NULL END) AS threeB,
    	        MAX(CASE WHEN b.category = 'OBP' THEN b.value ELSE NULL END) AS obp,
    	        MAX(CASE WHEN b.category = 'SLG' THEN b.value ELSE NULL END) AS slg,
    	        t.id AS teamId
    	    FROM Player p
    	    JOIN Team t ON p.teamId = t.id
    	    JOIN BatterStats b ON p.id = b.playerId
    	    WHERE b.season = :season
    	    GROUP BY p.name, t.name, t.logoName, p.id, b.position, t.id
    	""")
    	List<Object[]> findAllBatters(@Param("season") int season);
    	
//    	@Query(value = """
//    		    SELECT 
//    		        p.name AS playerName,
//    		        t.name AS teamName,
//    		        t.logo_name AS logoName,
//    		        t.id AS teamId,
//    		        MAX(CASE WHEN bs.category = 'AVG' THEN bs.value ELSE NULL END) AS avg,
//    		        MAX(CASE WHEN bs.category = 'OPS' THEN bs.value ELSE NULL END) AS ops,
//    		        MAX(CASE WHEN bs.category = 'WAR' THEN bs.value ELSE NULL END) AS war,
//    		        SUM(CASE WHEN bs.category = 'HR' THEN bs.value ELSE NULL END) AS hr,
//    		        SUM(CASE WHEN bs.category = 'SB' THEN bs.value ELSE NULL END) AS sb,
//    		        SUM(CASE WHEN bs.category = 'G' THEN bs.value ELSE NULL END) AS g,
//    		        SUM(CASE WHEN bs.category = 'PA' THEN bs.value ELSE NULL END) AS pa,
//    		        SUM(CASE WHEN bs.category = 'H' THEN bs.value ELSE NULL END) AS h,
//    		        SUM(CASE WHEN bs.category = 'RBI' THEN bs.value ELSE NULL END) AS rbi,
//    		        SUM(CASE WHEN bs.category = 'BB' THEN bs.value ELSE NULL END) AS bb,
//    		        SUM(CASE WHEN bs.category = 'SO' THEN bs.value ELSE NULL END) AS so,
//    		        SUM(CASE WHEN bs.category = '2B' THEN bs.value ELSE NULL END) AS twoB,
//    		        SUM(CASE WHEN bs.category = '3B' THEN bs.value ELSE NULL END) AS threeB,
//    		        MAX(CASE WHEN bs.category = 'OBP' THEN bs.value ELSE NULL END) AS obp,
//    		        MAX(CASE WHEN bs.category = 'SLG' THEN bs.value ELSE NULL END) AS slg,
//    		        MAX(CASE WHEN bs.category = 'wRC+' THEN bs.value ELSE NULL END) AS wrcPlus
//    		    FROM batter_stats bs
//    		    JOIN player p ON bs.player_id = p.id
//    		    JOIN team t ON p.team_id = t.id
//    		    WHERE bs.season = :season
//    		    GROUP BY bs.player_id
//    		    HAVING SUM(CASE WHEN bs.category = 'PA' THEN bs.value END) >= :requiredPA
//    		    """, nativeQuery = true)
//    		List<Object[]> findQualifiedBatters(@Param("season") int season, @Param("requiredPA") int requiredPA);
    	
}
