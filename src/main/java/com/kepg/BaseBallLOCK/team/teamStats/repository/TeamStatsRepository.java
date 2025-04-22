package com.kepg.BaseBallLOCK.team.teamStats.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kepg.BaseBallLOCK.team.teamStats.domain.TeamStats;

public interface TeamStatsRepository extends JpaRepository<TeamStats, Integer> {
    Optional<TeamStats> findByTeamIdAndSeasonAndCategory(Integer teamId, Integer season, String category);
    
    Optional<TeamStats> findByTeamIdAndSeasonAndCategory(int teamId, int season, String category);

}
