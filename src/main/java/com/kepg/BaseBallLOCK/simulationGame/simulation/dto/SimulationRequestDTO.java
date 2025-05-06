package com.kepg.BaseBallLOCK.simulationGame.simulation.dto;

import java.util.List;

import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulationRequestDTO {
    private List<PlayerCardOverallDTO> userLineup;
    private List<PlayerCardOverallDTO> botLineup;
    private String difficulty;
}
