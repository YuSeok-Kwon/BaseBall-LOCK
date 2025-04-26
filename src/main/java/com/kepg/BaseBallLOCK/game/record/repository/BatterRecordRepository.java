package com.kepg.BaseBallLOCK.game.record.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.game.record.domain.BatterRecord;

public interface BatterRecordRepository extends JpaRepository<BatterRecord, Integer> {
	
	List<BatterRecord> findByScheduleIdAndTeamId(int scheduleId, int teamId);
	
	boolean existsByScheduleIdAndTeamIdAndPlayerId(int scheduleId, int teamId, int playerId);
}

