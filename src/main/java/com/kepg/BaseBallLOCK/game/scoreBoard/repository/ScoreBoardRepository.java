package com.kepg.BaseBallLOCK.game.scoreBoard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kepg.BaseBallLOCK.game.scoreBoard.domain.ScoreBoard;

@Repository
public interface ScoreBoardRepository extends JpaRepository<ScoreBoard, Integer> {

    ScoreBoard findByScheduleId(Integer scheduleId);

    boolean existsByScheduleId(Integer scheduleId);
}
