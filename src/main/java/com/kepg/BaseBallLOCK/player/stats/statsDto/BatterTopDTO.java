package com.kepg.BaseBallLOCK.player.stats.statsDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatterTopDTO {
    private String position;   
    private String playerName;
    private String teamName;
    private String logoName;
    private double war;  
    private Double formattedValue;

}