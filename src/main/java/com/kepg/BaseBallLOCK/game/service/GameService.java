package com.kepg.BaseBallLOCK.game.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.team.domain.Team;
import com.kepg.BaseBallLOCK.team.service.TeamService;
import com.kepg.BaseBallLOCK.team.teamRanking.domain.TeamRanking;
import com.kepg.BaseBallLOCK.team.teamRanking.dto.TeamRankingCardView;
import com.kepg.BaseBallLOCK.team.teamRanking.service.TeamRankingService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final TeamRankingService teamRankingService;
    private final TeamService teamService;

    public List<TeamRankingCardView> getTeamRankingCardViews(int season) {
        List<TeamRanking> rankings = teamRankingService.getTeamRankings(season);
        List<TeamRankingCardView> cardViewList = new ArrayList<>();

        for (TeamRanking ranking : rankings) {
            Team team = teamService.getTeamById(ranking.getTeamId());
            if (team == null) continue;

            TeamRankingCardView cardView = TeamRankingCardView.builder()
                    .ranking(ranking.getRanking())
                    .games(ranking.getGames())
                    .wins(ranking.getWins())
                    .losses(ranking.getLosses())
                    .draws(ranking.getDraws())
                    .winRate(ranking.getWinRate())
                    .gamesBehind(ranking.getGamesBehind())
                    .teamName(team.getName())
                    .logoName(team.getLogoName())
                    .build();

            cardViewList.add(cardView);
        }

        return cardViewList;
    }

}
