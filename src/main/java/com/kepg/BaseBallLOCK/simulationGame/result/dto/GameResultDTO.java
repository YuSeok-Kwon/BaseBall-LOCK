package com.kepg.BaseBallLOCK.simulationGame.result.dto;

import java.util.List;

import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class GameResultDTO {
    private int scheduleId;
    private int userId;
    private int userScore;
    private int botScore;
    private boolean isWin;
    private String mvp;
    private List<String> gameLog; // 중계 메시지 모음
    private List<PlayerCardOverallDTO> botLineup;
}
