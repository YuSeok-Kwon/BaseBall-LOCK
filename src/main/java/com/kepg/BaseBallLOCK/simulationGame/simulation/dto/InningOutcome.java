package com.kepg.BaseBallLOCK.simulationGame.simulation.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class InningOutcome {
    private List<String> plays;
    private int score;
    private int nextBatterIndex;
}
