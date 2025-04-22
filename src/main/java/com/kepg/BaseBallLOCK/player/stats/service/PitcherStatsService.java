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
	    List<Object[]> results = playerService.getTopPitcherByTeamAndSeason(teamId, season);

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
	    enrichPitcherStats(view, playerId);

	    return view;
	}

	private void enrichPitcherStats(TopPlayerCardView view, int playerId) {
	    List<PitcherStats> stats = pitcherStatsRepository.findKeyStatsByPlayerId(playerId);
	    Map<String, PitcherStats> statMap = new HashMap<>();
	    for (PitcherStats stat : stats) {
	        statMap.put(stat.getCategory(), stat);
	    }

	    PitcherStats era = statMap.get("ERA");
	    PitcherStats whip = statMap.get("WHIP");
	    if (era != null) view.setEra(era.getValue());
	    if (whip != null) view.setWhip(whip.getValue());

	    int wins = 0;
	    int saves = 0;
	    int holds = 0;

	    if (statMap.containsKey("W")) {
	        wins = statMap.get("W").getValue().intValue();
	    }
	    if (statMap.containsKey("SV")) {
	        saves = statMap.get("SV").getValue().intValue();
	    }
	    if (statMap.containsKey("HOLD")) {
	        holds = statMap.get("HOLD").getValue().intValue();
	    }

	    if (wins >= saves && wins >= holds) {
	        view.setBestStatLabel("WINS");
	        view.setBestStatValue(wins);
	    } else if (saves >= wins && saves >= holds) {
	        view.setBestStatLabel("SAVES");
	        view.setBestStatValue(saves);
	    } else {
	        view.setBestStatLabel("HOLDS");
	        view.setBestStatValue(holds);
	    }
	}
}
