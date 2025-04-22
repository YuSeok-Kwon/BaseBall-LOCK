package com.kepg.BaseBallLOCK.player.stats.statsDto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PitcherStatsDTO {
    private int playerId;
    private int season;
    private String position;
    private String category;
    private double value;
    private Integer ranking;
}
