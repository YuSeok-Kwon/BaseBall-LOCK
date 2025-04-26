package com.kepg.BaseBallLOCK.team.teamStats.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.team.teamStats.domain.TeamStats;
import com.kepg.BaseBallLOCK.team.teamStats.dto.TopStatTeamDTO;
import com.kepg.BaseBallLOCK.team.teamStats.dto.TopStatTeamInterface;
import com.kepg.BaseBallLOCK.team.teamStats.repository.TeamStatsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamStatsService {

    private final TeamStatsRepository teamStatsRepository;

    // 기존 데이터가 있으면 업데이트, 없으면 생성
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

    // Interface -> DTO 변환 메서드
    private TopStatTeamDTO toDTO(TopStatTeamInterface projection) {
        return TopStatTeamDTO.builder()
                .category(projection.getCategory())
                .teamId(projection.getTeamId())
                .teamName(projection.getTeamName())
                .teamLogo(projection.getTeamLogo())
                .value(projection.getValue())
                .build();
    }

    public List<TopStatTeamDTO> getTopBatterStats(int season) {
        List<String> categories = Arrays.asList("OPS", "AVG", "HR", "SB", "BetterWAR");

        return categories.stream()
            .map(category -> teamStatsRepository.findTopByCategoryAndSeasonMax(season, category))
            .filter(Objects::nonNull) // null 값 제외
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<TopStatTeamDTO> getTopPitcherStats(int season) {
        List<TopStatTeamDTO> stats = new ArrayList<>();

        List<String> maxCategories = Arrays.asList("PitcherWAR", "SO", "W", "H", "SV");
        for (String category : maxCategories) {
            TopStatTeamInterface result = teamStatsRepository.findTopByCategoryAndSeasonMax(season, category);
            if (result != null) {
                stats.add(toDTO(result));
            }
        }

        List<String> minCategories = Arrays.asList("ERA", "WHIP");
        for (String category : minCategories) {
            TopStatTeamInterface result = teamStatsRepository.findTopByCategoryAndSeasonMin(season, category);
            if (result != null) {
                stats.add(toDTO(result));
            }
        }

        return stats;
    }

    public List<TopStatTeamDTO> getTopWaaStats(int season) {
        List<String> categories = Arrays.asList("타격", "주루", "수비", "선발", "불펜");

        return categories.stream()
            .map(category -> teamStatsRepository.findTopByCategoryAndSeasonMax(season, category))
            .filter(Objects::nonNull) // null 값 제외
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}