package com.kepg.BaseBallLOCK.player;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.player.domain.Player;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    
	@Query(value = """
		    SELECT p.name, p.position, s.value AS war, s.ranking, p.id
		    FROM stats s
		    JOIN player p ON s.playerId = p.id
		    WHERE s.category = 'WAR'
		      AND s.season = :season
		      AND p.teamId = :teamId
		      AND p.position != 'P'
		    ORDER BY s.value DESC
		    LIMIT 1
		    """, nativeQuery = true)
		List<Object[]> findTopHitterByTeamIdAndSeason(@Param("teamId") int teamId, @Param("season") int season);

		@Query(value = """
		    SELECT p.name, p.position, s.value AS war, s.ranking, p.id
		    FROM stats s
		    JOIN player p ON s.playerId = p.id
		    WHERE s.category = 'WAR'
		      AND s.season = :season
		      AND p.teamId = :teamId
		      AND p.position = 'P'
		    ORDER BY s.value DESC
		    LIMIT 1
		    """, nativeQuery = true)
		List<Object[]> findTopPitcherByTeamIdAndSeason(@Param("teamId") int teamId, @Param("season") int season);
}
