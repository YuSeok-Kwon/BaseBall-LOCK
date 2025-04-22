package com.kepg.BaseBallLOCK.game.lineUp.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.game.lineUp.domain.BatterLineup;
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

        BatterLineup lineup = BatterLineup.builder()
                .scheduleId(scheduleId)
                .teamId(teamId)
                .playerId(player.get().getId())
                .order(order)
                .position(position)
                .build();

        batterLineupRepository.save(lineup);
    }
}
