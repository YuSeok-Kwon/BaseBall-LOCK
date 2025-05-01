package com.kepg.BaseBallLOCK.player.stats;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kepg.BaseBallLOCK.player.stats.service.BatterStatsService;
import com.kepg.BaseBallLOCK.player.stats.service.PitcherStatsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ranking")
public class PlayerStatsController {

    private final BatterStatsService batterStatsService;
    private final PitcherStatsService pitcherStatsService;

    @GetMapping("/playerranking-view")
    public String playerRankingView(
            @RequestParam(name = "season", required = false, defaultValue = "2025") int season,
            @RequestParam(name = "sort", required = false, defaultValue = "WAR") String sort,
            @RequestParam(name = "direction", required = false, defaultValue = "DESC") String direction,
            Model model) {

        // 데이터를 모델에 추가
        model.addAttribute("season", season);
        model.addAttribute("topBatters", batterStatsService.getTopBattersByPosition(season));
        model.addAttribute("topPitchers", pitcherStatsService.getTopPitchers(season));
        model.addAttribute("batterRankingList", batterStatsService.getPlayerRankingsSorted(season, sort, direction));
        model.addAttribute("pitcherRankingList", pitcherStatsService.getPitcherRankingsSorted(season, sort, direction));
        model.addAttribute("currentSort", sort.trim().toUpperCase());
        model.addAttribute("sortDirection", direction);

        return "ranking/playerranking";
    }
}