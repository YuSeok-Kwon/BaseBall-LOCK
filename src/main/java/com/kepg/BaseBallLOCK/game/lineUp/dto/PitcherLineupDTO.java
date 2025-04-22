package com.kepg.BaseBallLOCK.game.lineUp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PitcherLineupDTO {
    private int scheduleId;
    private int teamId;
    private int playerId;
    private int order;
    private String position;
}
