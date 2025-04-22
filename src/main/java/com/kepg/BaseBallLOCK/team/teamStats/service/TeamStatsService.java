package com.kepg.BaseBallLOCK.team.teamStats.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.team.teamStats.domain.TeamStats;
import com.kepg.BaseBallLOCK.team.teamStats.repository.TeamStatsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamStatsService {

    private final TeamStatsRepository teamStatsRepository;

    public void saveOrUpdate(int teamId, int season, String category, double value, Integer ranking) {
        Optional<TeamStats> optional = teamStatsRepository.findByTeamIdAndSeasonAndCategory(teamId, season, category);
        TeamStats stat;
        if (optional.isPresent()) {
            stat = optional.get();
            stat.setValue(value);
            stat.setRanking(ranking);
        } else {
            stat = TeamStats.builder()
                    .teamId(teamId)
                    .season(season)
                    .category(category)
                    .value(value)
                    .ranking(ranking)
                    .build();
        }
        teamStatsRepository.save(stat);
    }
}
