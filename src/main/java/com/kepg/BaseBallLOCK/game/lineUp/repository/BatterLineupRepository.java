package com.kepg.BaseBallLOCK.game.lineUp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kepg.BaseBallLOCK.game.lineUp.domain.BatterLineup;

@Repository
public interface BatterLineupRepository extends JpaRepository<BatterLineup, Integer> {
	
	List<BatterLineup> findByScheduleIdAndTeamId(int scheduleId, int teamId);
	
	boolean existsByScheduleIdAndTeamIdAndPlayerId(int scheduleId, int teamId, int playerId);
	
	@Query("SELECT DISTINCT b.player.name FROM BatterLineup b WHERE b.scheduleId = :scheduleId AND b.teamId = :teamId")
	List<String> findBatterNamesByScheduleIdAndTeamId(@Param("scheduleId") int scheduleId,
	                                                  @Param("teamId") int teamId);
}
