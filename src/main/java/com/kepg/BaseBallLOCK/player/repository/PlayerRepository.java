package com.kepg.BaseBallLOCK.player.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.player.domain.Player;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    
	@Query(value = """
		    SELECT p.name, s.position, s.value AS war, s.ranking, p.id
		    FROM batterStats s
		    JOIN player p ON s.playerId = p.id
		    WHERE s.category = 'WAR'
		      AND s.season = :season
		      AND p.teamId = :teamId
		      AND s.position != 'P'
		    ORDER BY s.value DESC
		    LIMIT 1
		    """, nativeQuery = true)
		List<Object[]> findTopHitterByTeamIdAndSeason(@Param("teamId") int teamId, @Param("season") int season);

	@Query(value = """
	    SELECT p.name, s.position, s.value AS war, s.ranking, p.id
	    FROM pitcherStats s
	    JOIN player p ON s.playerId = p.id
	    WHERE s.category = 'WAR'
	      AND s.season = :season
	      AND p.teamId = :teamId
	      AND s.position = 'P'
	    ORDER BY s.value DESC
	    LIMIT 1
	    """, nativeQuery = true)
	List<Object[]> findTopPitcherByTeamIdAndSeason(@Param("teamId") int teamId, @Param("season") int season);
	
	@Query("SELECT s.value FROM BatterStats s WHERE s.playerId = :playerId AND s.category = :category AND s.season = :season")
	Optional<String> findStatValueByPlayerIdCategoryAndSeason(@Param("playerId") int playerId, @Param("category") String category, @Param("season") int season);
	
	Optional<Player> findByNameAndTeamId(String name, int teamId);
}
