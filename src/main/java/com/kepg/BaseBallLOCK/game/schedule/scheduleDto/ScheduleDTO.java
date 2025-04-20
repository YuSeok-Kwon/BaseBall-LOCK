package com.kepg.BaseBallLOCK.game.schedule.scheduleDto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data 
@NoArgsConstructor 
@AllArgsConstructor
@Getter
@Setter
public class ScheduleDTO {
	
    private int id;
    private Timestamp matchDate;
    private int homeTeamId;
    private int awayTeamId;
    private Integer homeTeamScore;
    private Integer awayTeamScore;
    private String stadium;
    private String status;
    
}
