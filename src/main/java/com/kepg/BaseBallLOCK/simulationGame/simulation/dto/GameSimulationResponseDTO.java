package com.kepg.BaseBallLOCK.simulationGame.simulation.dto;

import java.util.List;

import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameSimulationResponseDTO {
    private List<InningResultDTO> results;
    private List<PlayerCardOverallDTO> userLineup;
    private List<PlayerCardOverallDTO> botLineup;
}
