package com.kepg.BaseBallLOCK.game.record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kepg.BaseBallLOCK.game.record.domain.PitcherRecord;

@Repository
public interface PitcherRecordRepository extends JpaRepository<PitcherRecord, Integer> {
	
	List<PitcherRecord> findByScheduleIdAndTeamId(int scheduleId, int teamId);
	
	boolean existsByScheduleIdAndTeamIdAndPlayerId(int scheduleId, int teamId, int playerId);
	
	List<PitcherRecord> findByScheduleId(int scheduleId);
	
}
