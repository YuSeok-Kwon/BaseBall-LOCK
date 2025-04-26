package com.kepg.BaseBallLOCK.player.stats.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.player.dto.TopPlayerCardView;
import com.kepg.BaseBallLOCK.player.service.PlayerService;
import com.kepg.BaseBallLOCK.player.stats.domain.BatterStats;
import com.kepg.BaseBallLOCK.player.stats.repository.BatterStatsRepository;
import com.kepg.BaseBallLOCK.player.stats.statsDto.BatterStatsDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatterStatsService {

    private final BatterStatsRepository batterStatsRepository;
    private final PlayerService playerService; 
    

    public void saveBatterStats(BatterStatsDTO dto) {
        Optional<BatterStats> optional = batterStatsRepository
                .findByPlayerIdAndSeasonAndCategory(dto.getPlayerId(), dto.getSeason(), dto.getCategory());

        BatterStats entity;
        if (optional.isPresent()) {
            entity = optional.get();
            entity.setValue(dto.getValue());
            entity.setRanking(dto.getRanking());
            entity.setPosition(dto.getPosition());
        } else {
            entity = BatterStats.builder()
                    .playerId(dto.getPlayerId())
                    .season(dto.getSeason())
                    .position(dto.getPosition())
                    .category(dto.getCategory())
                    .value(dto.getValue())
                    .ranking(dto.getRanking())
                    .build();
        }

        batterStatsRepository.save(entity);
    }
    
    public TopPlayerCardView getTopHitter(int teamId, int season) {
        List<Object[]> result = playerService.getTopHitterByTeamAndSeason(teamId, season);
        if (result.isEmpty()) {
            return null;
        }

        Object[] row = result.get(0);

        String name = (String) row[0];
        String position = (String) row[1];
        double war = 0.0;
        int ranking = 0;
        int playerId = 0;

        if (row[2] != null) {
            war = Double.parseDouble(row[2].toString());
        }

        if (row[3] != null) {
            ranking = Integer.parseInt(row[3].toString());
        }

        if (row[4] != null) {
            playerId = Integer.parseInt(row[4].toString());
        }

        double avg = -1.0;
        int hr = 1;
        double ops = -1.0;

        Optional<String> avgOptional = batterStatsRepository.findStatValueByPlayerIdCategoryAndSeason(playerId, "AVG", season);
        if (avgOptional.isPresent()) {
            avg = Double.parseDouble(avgOptional.get());
        }

        Optional<String> hrOptional = batterStatsRepository.findStatValueByPlayerIdCategoryAndSeason(playerId, "HR", season);
        if (hrOptional.isPresent()) {
            String rawHr = hrOptional.get().trim();
            try {
                hr = (int) Double.parseDouble(rawHr);
            } catch (NumberFormatException e) {
                System.out.println("⚠️ HR 변환 오류: " + rawHr);
            }
        }

        Optional<String> opsOptional = batterStatsRepository.findStatValueByPlayerIdCategoryAndSeason(playerId, "OPS", season);
        if (opsOptional.isPresent()) {
            ops = Double.parseDouble(opsOptional.get());
        }

        return TopPlayerCardView.builder()
                .name(name)
                .position(position)
                .war(war)
                .warRank(ranking)
                .avg(avg)
                .hr(hr)
                .ops(ops)
                .build();
    }
}