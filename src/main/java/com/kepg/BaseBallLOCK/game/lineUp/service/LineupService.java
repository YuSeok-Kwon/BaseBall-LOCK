package com.kepg.BaseBallLOCK.game.lineUp.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.game.lineUp.domain.BatterLineup;
import com.kepg.BaseBallLOCK.game.lineUp.dto.BatterLineupDTO;
import com.kepg.BaseBallLOCK.game.lineUp.repository.BatterLineupRepository;
import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.service.PlayerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LineupService {

    private final BatterLineupRepository batterLineupRepository;
    private final PlayerService playerService;

    public void saveBatterLineup(int scheduleId, int teamId, int order, String position, String playerName) {
        Optional<Player> player = playerService.findByNameAndTeamId(playerName, teamId);
        if (player.isEmpty()) return;

        int playerId = player.get().getId();

        boolean exists = batterLineupRepository.existsByScheduleIdAndTeamIdAndPlayerId(scheduleId, teamId, playerId);
        if (exists) return;

        BatterLineup lineup = BatterLineup.builder()
                .scheduleId(scheduleId)
                .teamId(teamId)
                .playerId(playerId)
                .order(order)
                .position(position)
                .build();

        batterLineupRepository.save(lineup);
    }
    
    public List<BatterLineupDTO> getBatterLineup(int scheduleId, int teamId) {
        List<BatterLineup> entities = batterLineupRepository.findByScheduleIdAndTeamId(scheduleId, teamId);
        List<BatterLineupDTO> dtoList = new ArrayList<>();

        for (BatterLineup entity : entities) {
            Player player = entity.getPlayer();
            Integer playerId = null;
            String playerName = null;

            if (player != null) {
                playerId = player.getId();
                playerName = player.getName();
            } else {
                playerId = entity.getPlayerId();
                playerName = "알 수 없음";
            }

            BatterLineupDTO dto = BatterLineupDTO.builder()
                .order(entity.getOrder())
                .position(entity.getPosition())
                .playerId(playerId)
                .playerName(playerName)
                .build();

            dtoList.add(dto);
        }

        return dtoList;
    }
    
    public List<String> getBatterNamesByScheduleId(int scheduleId, int teamId) {
        return batterLineupRepository.findBatterNamesByScheduleIdAndTeamId(scheduleId, teamId);
    }
}
