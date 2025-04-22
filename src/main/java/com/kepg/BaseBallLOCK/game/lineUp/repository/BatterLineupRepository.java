package com.kepg.BaseBallLOCK.game.lineUp.repository;

import com.kepg.BaseBallLOCK.game.lineUp.domain.BatterLineup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatterLineupRepository extends JpaRepository<BatterLineup, Integer> {
}
