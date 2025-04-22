package com.kepg.BaseBallLOCK.game.record.repository;

import com.kepg.BaseBallLOCK.game.record.domain.PitcherRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitcherRecordRepository extends JpaRepository<PitcherRecord, Integer> {
}
