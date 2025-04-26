package com.kepg.BaseBallLOCK.team.teamRanking;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kepg.BaseBallLOCK.game.service.GameService;
import com.kepg.BaseBallLOCK.team.teamRanking.dto.TeamRankingCardView;
import com.kepg.BaseBallLOCK.team.teamStats.dto.TopStatTeamDTO;
import com.kepg.BaseBallLOCK.team.teamStats.service.TeamStatsService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/ranking")
@Controller
@RequiredArgsConstructor
public class TeamRankingController {
	
	private final TeamStatsService teamStatsService;
	private final GameService gameService;

	@GetMapping("/teamranking-view")
	public String teamRankingView(Model model) {
	    int season = 2025;

	    List<TopStatTeamDTO> batterStats = teamStatsService.getTopBatterStats(season);
	    List<TopStatTeamDTO> pitcherStats = teamStatsService.getTopPitcherStats(season);
	    List<TopStatTeamDTO> waaStats = teamStatsService.getTopWaaStats(season);

	    model.addAttribute("topBatterStats", batterStats);
	    model.addAttribute("topPitcherStats", pitcherStats);
	    model.addAttribute("topWaaStats", waaStats);

	    // 팀 순위 카드
	    List<TeamRankingCardView> rankingList = gameService.getTeamRankingCardViews(season);
	    model.addAttribute("rankingList", rankingList);

	    return "ranking/teamranking";
	}
    
}
