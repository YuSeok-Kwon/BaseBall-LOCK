package com.kepg.BaseBallLOCK.game.record.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.game.record.domain.BatterRecord;

public interface BatterRecordRepository extends JpaRepository<BatterRecord, Integer> {
}

