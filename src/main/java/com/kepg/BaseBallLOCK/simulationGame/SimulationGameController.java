package com.kepg.BaseBallLOCK.simulationGame;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.gson.Gson;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;
import com.kepg.BaseBallLOCK.simulationGame.result.dto.GameResultDTO;
import com.kepg.BaseBallLOCK.simulationGame.result.service.GameResultService;
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
	private final GameResultService gameResultService;
		
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
	    int userId = (Integer) session.getAttribute("userId");

	    List<PlayerCardOverallDTO> userLineup = userLineupService.getSavedLineup(userId);

	    List<PlayerCardOverallDTO> botLineup = (List<PlayerCardOverallDTO>) session.getAttribute("botLineup");    
	    
	    int scheduleId = simulationGameService.createSimulationSchedule(userId, difficulty);
	    SimulationResultDTO result = simulationService.playSimulationGame(userLineup, botLineup, difficulty);
	    GameResultDTO summary = gameResultService.generateGameSummary(scheduleId, userId, result, userLineup, botLineup);
	    gameResultService.saveGameResult(summary);

	    model.addAttribute("userId", userId);
	    model.addAttribute("scheduleId", scheduleId);
	    model.addAttribute("difficulty", difficulty);
	    model.addAttribute("botLineup", botLineup);
	    
	    Gson gson = new Gson();

	    model.addAttribute("botLineup", gson.toJson(botLineup));
	    model.addAttribute("userLineup", gson.toJson(userLineup));
	    model.addAttribute("result", gson.toJson(result.getInnings()));

	    return "game/play";
	}
}
