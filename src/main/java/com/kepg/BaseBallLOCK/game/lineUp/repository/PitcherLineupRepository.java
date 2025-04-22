package com.kepg.BaseBallLOCK.game.lineUp.repository;

import com.kepg.BaseBallLOCK.game.lineUp.domain.PitcherLineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PitcherLineupRepository extends JpaRepository<PitcherLineup, Integer> {
}
