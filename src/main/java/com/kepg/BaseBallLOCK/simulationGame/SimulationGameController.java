package com.kepg.BaseBallLOCK.simulationGame;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;
import com.kepg.BaseBallLOCK.simulationGame.service.SimulationGameService;
import com.kepg.BaseBallLOCK.simulationGame.simulation.dto.SimulationResultDTO;
import com.kepg.BaseBallLOCK.simulationGame.simulation.service.SimulationService;
import com.kepg.BaseBallLOCK.simulationGame.userLineup.service.UserLineupService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/game")
@Controller
public class SimulationGameController {
	
	private final SimulationGameService simulationGameService;
	private final SimulationService simulationService;
	private final UserLineupService userLineupService;
		
	@GetMapping("/home-view")
	public String gameHomeView() {
		return "game/home";
	}
	
	@GetMapping("/ready-view")
	public String gameReadyView(HttpSession session) {

		return "game/ready";
	}
	
	@GetMapping("/play-view")
	public String showResult(@RequestParam(defaultValue = "normal") String difficulty, Model model, HttpSession session) {		
		int userId = (Integer)session.getAttribute("userId");
		
		//  유저 라인업 불러오기
        List<PlayerCardOverallDTO> userLineup = userLineupService.getSavedLineup(userId);

        //  봇 라인업 생성 (normal 난이도)
        List<PlayerCardOverallDTO> botLineup = simulationGameService.generateBotLineupWithStats(difficulty);

        //  전체 경기 시뮬레이션 실행
        SimulationResultDTO result = simulationService.playSimulationGame(userLineup, botLineup, difficulty);

        model.addAttribute("userId", userId);
        model.addAttribute("difficulty", difficulty);
        model.addAttribute("result", result);
        model.addAttribute("userLineup", userLineup);
        model.addAttribute("botLineup", botLineup);

        return "game/play";
	}
}
