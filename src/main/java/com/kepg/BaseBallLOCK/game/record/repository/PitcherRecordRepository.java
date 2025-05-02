package com.kepg.BaseBallLOCK.game.record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kepg.BaseBallLOCK.game.record.domain.PitcherRecord;

@Repository
public interface PitcherRecordRepository extends JpaRepository<PitcherRecord, Integer> {
	
	List<PitcherRecord> findByScheduleIdAndTeamId(int scheduleId, int teamId);
	
	boolean existsByScheduleIdAndTeamIdAndPlayerId(int scheduleId, int teamId, int playerId);
	
	List<PitcherRecord> findByScheduleId(int scheduleId);
	
	@Query("SELECT DISTINCT p.player.name FROM PitcherRecord p WHERE p.scheduleId = :scheduleId AND p.teamId = :teamId")
	List<String> findPitcherNamesByScheduleIdAndTeamId(@Param("scheduleId") int scheduleId,
	                                                   @Param("teamId") int teamId);
}
