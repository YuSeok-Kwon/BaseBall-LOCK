package com.kepg.BaseBallLOCK.player.stats.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        List<Object[]> results = playerService.getTopHitterByTeamAndSeason(teamId, season);

        if (results == null || results.isEmpty()) {
            return null;
        }

        Object[] result = results.get(0);
        if (result.length < 5) return null;

        TopPlayerCardView view = TopPlayerCardView.builder()
                .name((String) result[0])
                .position((String) result[1])
                .war((Double) result[2])
                .warRank(result[3] != null ? (Integer) result[3] : 0)
                .build();

        Integer playerId = (Integer) result[4];
        enrichHitterStats(view, playerId);

        return view;
    }

    private void enrichHitterStats(TopPlayerCardView view, int playerId) {
        List<BatterStats> stats = batterStatsRepository.findKeyStatsByPlayerId(playerId);
        Map<String, BatterStats> statMap = new HashMap<>();
        for (BatterStats stat : stats) {
            statMap.put(stat.getCategory(), stat);
        }

        BatterStats avg = statMap.get("AVG");
        BatterStats hr = statMap.get("HR");
        BatterStats ops = statMap.get("OPS");

        if (avg != null) view.setAvg(avg.getValue());
        if (hr != null) view.setHr(hr.getValue().intValue());
        if (ops != null) view.setOps(ops.getValue());
    }
}