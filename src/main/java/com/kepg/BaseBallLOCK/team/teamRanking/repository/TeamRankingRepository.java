package com.kepg.BaseBallLOCK.team.teamRanking.repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.team.teamRanking.domain.TeamRanking;

public interface TeamRankingRepository extends JpaRepository<TeamRanking, Integer> {
	
    List<TeamRanking> findBySeasonOrderByRankingAsc(int season);
    
    Optional<TeamRanking> findBySeasonAndTeamId(int season, int teamId);

}
