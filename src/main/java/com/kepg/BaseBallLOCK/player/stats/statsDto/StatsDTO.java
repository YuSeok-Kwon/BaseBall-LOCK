package com.kepg.BaseBallLOCK.player.stats.statsDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class StatsDTO {
    private int id;
    private int plyerId;
    private String season;
    private String category;
    private String value;          
    private String rank;               
}
