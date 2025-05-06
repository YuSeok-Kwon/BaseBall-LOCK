package com.kepg.BaseBallLOCK.simulationGame.dto;

import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;
import com.kepg.BaseBallLOCK.simulationGame.userLineup.dto.UserLineupDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameReadyCardView {
    private UserLineupDTO lineup;          // 포지션, 순번, 시즌 정보
    private PlayerCardOverallDTO card;     // 해당 선수의 카드 정보
}
