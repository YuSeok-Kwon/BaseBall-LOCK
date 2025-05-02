package com.kepg.BaseBallLOCK.game.highlight.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.game.highlight.doamin.GameHighlight;

public interface GameHighlightRepository extends JpaRepository<GameHighlight, Integer> {
	
    GameHighlight findByScheduleIdAndRanking(Integer scheduleId, Integer ranking);
    
    List<GameHighlight> findByScheduleIdOrderByRankingAsc(Integer scheduleId);

}