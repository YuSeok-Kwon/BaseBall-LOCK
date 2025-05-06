package com.kepg.BaseBallLOCK.simulationGame.simulation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameStartRequestDTO {
 private Integer userId;
 private String difficulty;
}
