package com.kepg.BaseBallLOCK.player.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.dto.PlayerDTO;
import com.kepg.BaseBallLOCK.player.repository.PlayerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public Optional<Player> findByNameAndTeamId(String name, int teamId) {
        return playerRepository.findByNameAndTeamId(name, teamId);
    }

    public Player findOrCreatePlayer(PlayerDTO dto) {
        Optional<Player> existing = playerRepository.findByNameAndTeamId(dto.getName(), dto.getTeamId());
        if (existing.isPresent()) {
        	return existing.get();
        }
        Player newPlayer = Player.builder()
                .name(dto.getName())
                .teamId(dto.getTeamId())
                .build();
        return playerRepository.save(newPlayer);
    }
    
    public List<Object[]> getTopPitcherByTeamAndSeason(int teamId, int season) {
        return playerRepository.findTopPitcherByTeamIdAndSeason(teamId, season);
    }

    public List<Object[]> getTopHitterByTeamAndSeason(int teamId, int season) {
        return playerRepository.findTopHitterByTeamIdAndSeason(teamId, season);
    }
}
