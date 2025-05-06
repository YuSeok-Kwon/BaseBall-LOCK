package com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerCardSaveRequest {
    private Integer playerId;
    private Integer season;
    private String grade;
    private String position;
}
