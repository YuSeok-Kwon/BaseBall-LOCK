package com.kepg.BaseBallLOCK.player.stats.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.player.stats.domain.PitcherStats;

public interface PitcherStatsRepository extends JpaRepository<PitcherStats, Integer> {
	
    Optional<PitcherStats> findByPlayerIdAndSeasonAndCategory(Integer playerId, Integer season, String category);
    
    @Query("SELECT s.value FROM PitcherStats s WHERE s.playerId = :playerId AND s.category = :category AND s.season = :season")
    Optional<String> findStatValueByPlayerIdCategoryAndSeason(@Param("playerId") int playerId, @Param("category") String category, @Param("season") int season);

    // 투수 기록별 1등 리스트
    @Query(value = """
    	    WITH ranked_pitchers AS (
    	        SELECT 
    	            bs.position AS position,
    	            p.name AS playerName,
    	            t.name AS teamName,
    	            t.logoName AS logoName,
    	            COALESCE(bs.value, 0) AS value,
    	            bs.category,
    	            ROW_NUMBER() OVER (
    	                PARTITION BY bs.position, bs.category 
    	                ORDER BY 
    	                    CASE 
    	                        WHEN bs.category = 'ERA' THEN bs.value
    	                        WHEN bs.category = 'WHIP' THEN bs.value
    	                        WHEN bs.category IN ('G', 'IP', 'W', 'HLD', 'SV', 'SO', 'WAR') THEN -bs.value 
    	                        ELSE bs.value
    	                    END
    	            ) AS row_num
    	        FROM pitcherStats bs
    	        JOIN player p ON bs.playerId = p.id
    	        JOIN team t ON p.teamId = t.id
    	        WHERE bs.season = :season
    	          AND bs.category IN ('ERA', 'WHIP', 'G', 'IP', 'W', 'HLD', 'SV', 'SO', 'WAR')
    	          AND (bs.category != 'ERA' OR bs.value > 0)
    	    )
    	    SELECT 
    	        position,
    	        playerName,
    	        teamName,
    	        logoName,
    	        value AS record_value,
    	        category
    	    FROM ranked_pitchers
    	    WHERE row_num = 1
    	    ORDER BY position, category;
    	""", nativeQuery = true)
    	List<Object[]> findTopPitchersAsTuple(@Param("season") int season);

    	// 전체 투수 랭킹
    	@Query("""
    		    SELECT 
    		        p.name AS playerName,
    		        t.name AS teamName,
    		        t.logoName AS logoName,
    		        MAX(CASE WHEN b.category = 'ERA' THEN b.value ELSE NULL END) AS era,
    		        MAX(CASE WHEN b.category = 'WHIP' THEN b.value ELSE NULL END) AS whip,
    		        MAX(CASE WHEN b.category = 'W' THEN b.value ELSE NULL END) AS wins,
    		        MAX(CASE WHEN b.category = 'L' THEN b.value ELSE NULL END) AS losses,
    		        MAX(CASE WHEN b.category = 'SV' THEN b.value ELSE NULL END) AS saves,
    		        MAX(CASE WHEN b.category = 'HLD' THEN b.value ELSE NULL END) AS holds,
    		        MAX(CASE WHEN b.category = 'SO' THEN b.value ELSE NULL END) AS strikeouts,
    		        MAX(CASE WHEN b.category = 'BB' THEN b.value ELSE NULL END) AS walks,
    		        MAX(CASE WHEN b.category = 'H' THEN b.value ELSE NULL END) AS hitsAllowed,
    		        MAX(CASE WHEN b.category = 'HR' THEN b.value ELSE NULL END) AS homeRunsAllowed,
    		        MAX(CASE WHEN b.category = 'IP' THEN b.value ELSE NULL END) AS inningsPitched,
    		        MAX(CASE WHEN b.category = 'WAR' THEN b.value ELSE NULL END) AS war,
    		        t.id AS teamId
    		    FROM Player p
    		    JOIN Team t ON p.teamId = t.id
    		    JOIN PitcherStats b ON p.id = b.playerId
    		    WHERE b.season = :season
    		    GROUP BY p.name, t.name, t.logoName, t.id
    		""")
    		List<Object[]> findAllPitchers(@Param("season") int season);
    	
//    		@Query(value = """
//    			    SELECT 
//    					   p.name AS playerName,
//    					   t.name AS teamName,
//    			           t.logoName AS logoName,
//    			           t.id AS teamId,
//		    		        MAX(CASE WHEN b.category = 'ERA' THEN b.value ELSE NULL END) AS era,
//		    		        MAX(CASE WHEN b.category = 'WHIP' THEN b.value ELSE NULL END) AS whip,
//		    		        MAX(CASE WHEN b.category = 'W' THEN b.value ELSE NULL END) AS wins,
//		    		        MAX(CASE WHEN b.category = 'L' THEN b.value ELSE NULL END) AS losses,
//		    		        MAX(CASE WHEN b.category = 'SV' THEN b.value ELSE NULL END) AS saves,
//		    		        MAX(CASE WHEN b.category = 'HLD' THEN b.value ELSE NULL END) AS holds,
//		    		        MAX(CASE WHEN b.category = 'SO' THEN b.value ELSE NULL END) AS strikeouts,
//		    		        MAX(CASE WHEN b.category = 'BB' THEN b.value ELSE NULL END) AS walks,
//		    		        MAX(CASE WHEN b.category = 'H' THEN b.value ELSE NULL END) AS hitsAllowed,
//		    		        MAX(CASE WHEN b.category = 'HR' THEN b.value ELSE NULL END) AS homeRunsAllowed,
//		    		        MAX(CASE WHEN b.category = 'IP' THEN b.value ELSE NULL END) AS inningsPitched,
//		    		        MAX(CASE WHEN b.category = 'WAR' THEN b.value ELSE NULL END) AS war
//    			    FROM pitcherStats ps
//    			    JOIN player p ON ps.player_id = p.id
//    			    JOIN team t ON p.team_id = t.id
//    			    WHERE ps.season = :season
//    			    GROUP BY ps.player_id
//    			    HAVING SUM(CASE WHEN ps.category = 'IP' THEN ps.value END) >= :requiredIP
//    			""", nativeQuery = true)
//    			List<Object[]> findQualifiedPitchers(@Param("season") int season, @Param("requiredIP") int requiredIP);
    			
    		
}
