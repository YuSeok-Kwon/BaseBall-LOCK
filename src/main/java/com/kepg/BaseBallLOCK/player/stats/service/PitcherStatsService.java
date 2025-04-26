package com.kepg.BaseBallLOCK.player.stats.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.player.dto.TopPlayerCardView;
import com.kepg.BaseBallLOCK.player.service.PlayerService;
import com.kepg.BaseBallLOCK.player.stats.domain.PitcherStats;
import com.kepg.BaseBallLOCK.player.stats.repository.PitcherStatsRepository;
import com.kepg.BaseBallLOCK.player.stats.statsDto.PitcherStatsDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PitcherStatsService {
	
	private final PitcherStatsRepository pitcherStatsRepository;
	private final PlayerService playerService; 
	
	public void savePitcherStats(PitcherStatsDTO dto) {
		
        Optional<PitcherStats> optional = pitcherStatsRepository
                .findByPlayerIdAndSeasonAndCategory(dto.getPlayerId(), dto.getSeason(), dto.getCategory());

        PitcherStats entity;
        if (optional.isPresent()) {
            entity = optional.get();
            entity.setValue(dto.getValue());
            entity.setRanking(dto.getRanking());
            entity.setPosition(dto.getPosition());
        } else {
            entity = PitcherStats.builder()
                    .playerId(dto.getPlayerId())
                    .season(dto.getSeason())
                    .position(dto.getPosition())
                    .category(dto.getCategory())
                    .value(dto.getValue())
                    .ranking(dto.getRanking())
                    .position(dto.getPosition()) 
                    .build();
        }

        pitcherStatsRepository.save(entity);
    }
	
	public TopPlayerCardView getTopPitcher(int teamId, int season) {
	    List<Object[]> result = playerService.getTopPitcherByTeamAndSeason(teamId, season);
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

	    double era = -1.0;
	    double whip = -1.0;
	    String bestStatLabel = "-";
	    int bestStatValue = -1;

	    Optional<String> eraOpt = pitcherStatsRepository.findStatValueByPlayerIdCategoryAndSeason(playerId, "ERA", season);
	    if (eraOpt.isPresent()) {
	        era = Double.parseDouble(eraOpt.get());
	    }

	    Optional<String> whipOpt = pitcherStatsRepository.findStatValueByPlayerIdCategoryAndSeason(playerId, "WHIP", season);
	    if (whipOpt.isPresent()) {
	        whip = Double.parseDouble(whipOpt.get());
	    }

	    Map<String, Integer> statMap = new HashMap<>();
	    for (String cat : List.of("W", "SV", "HLD")) {
	        Optional<String> statOpt = pitcherStatsRepository.findStatValueByPlayerIdCategoryAndSeason(playerId, cat, season);
	        if (statOpt.isPresent()) {
	            int statValue = (int) Double.parseDouble(statOpt.get());
	            statMap.put(cat, statValue);
	        }
	    }

	    for (Map.Entry<String, Integer> entry : statMap.entrySet()) {
	        if (entry.getValue() > bestStatValue) {
	            bestStatLabel = entry.getKey();
	            bestStatValue = entry.getValue();
	        }
	    }

	    return TopPlayerCardView.builder()
	            .name(name)
	            .position(position)
	            .war(war)
	            .warRank(ranking)
	            .era(era)
	            .whip(whip)
	            .bestStatLabel(bestStatLabel)
	            .bestStatValue(bestStatValue)
	            .build();
	}

	
}
