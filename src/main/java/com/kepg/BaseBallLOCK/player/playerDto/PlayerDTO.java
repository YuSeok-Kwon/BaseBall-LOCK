package com.kepg.BaseBallLOCK.player.playerDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class PlayerDTO {
    private int id;
    private int teamId;
    private String name;
    private String position;
    private String birth;          
    private String hand;               
    private Integer backNumber;
    private int season;
}