package com.kepg.BaseBallLOCK.team.teamRanking.teamRankingDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class TeamRankingDTO {

	int teamId;
	int season;
	int games;
	int ranking;
	int wins;
	int losses;
	int draws;
	double winRate;
	Double gamesBehind;
}
