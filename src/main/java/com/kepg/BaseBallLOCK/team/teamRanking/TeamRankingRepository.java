package com.kepg.BaseBallLOCK.team.teamRanking;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.team.teamRanking.domain.TeamRanking;

public interface TeamRankingRepository extends JpaRepository<TeamRanking, Integer> {
	
    List<TeamRanking> findBySeasonOrderByRankingAsc(int season);
}
