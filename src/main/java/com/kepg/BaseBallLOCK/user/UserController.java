package com.kepg.BaseBallLOCK.user;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.kepg.BaseBallLOCK.game.GameService;
import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;
import com.kepg.BaseBallLOCK.player.playerDto.TopPlayerCardView;
import com.kepg.BaseBallLOCK.team.teamDomain.Team;
import com.kepg.BaseBallLOCK.team.teamRanking.teamRankingDto.TeamRankingCardView;
import com.kepg.BaseBallLOCK.user.userDomain.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RequestMapping("/user")
@Controller
@RequiredArgsConstructor
public class UserController {
	
	private final GameService gameService;

	@GetMapping("/login-view")
	public String loginView() {
		return "user/login";
	}
	
	@GetMapping("/join-view")
	public String joinView() {
		return "user/join";
	}
	
	@GetMapping("/find-id-view")
	public String findIdView() {
		return "user/findId";
	}
	
	@GetMapping("/find-password-view")
	public String findPasswordView() {
		return "user/findPassword";
	}
	
	@GetMapping("/home")
	public String homeView(Model model, HttpSession session) {
	    User user = (User) session.getAttribute("loginUser");

	    if (user == null) {
	        return "redirect:/user/login-view"; // 로그인 안 된 경우 로그인 페이지로 이동
	    }

	    int myTeamId = user.getFavoriteTeamId();
	    int season = LocalDate.now().getYear();

	    // My Team
	    Team myTeam = gameService.getTeamInfo(myTeamId);
	    model.addAttribute("myTeam", myTeam);

	    // 오늘 경기
	    Schedule schedule = gameService.getTodayScheduleByTeam(myTeamId);
	    model.addAttribute("schedule", schedule);

	    if (schedule != null) {
	        int opponentId = 0;

	        if (schedule.getHomeTeamId() == myTeamId) {
	            opponentId = schedule.getAwayTeamId();
	        } else {
	            opponentId = schedule.getHomeTeamId();
	        }

	        Team opponentTeam = gameService.getTeamInfo(opponentId);
	        model.addAttribute("opponentTeam", opponentTeam);

	        // 최근 전적
	        List<String> myRecentResults = gameService.getRecentResults(myTeamId);
	        List<String> opponentRecentResults = gameService.getRecentResults(opponentId);

	        model.addAttribute("myRecentResults", myRecentResults);
	        model.addAttribute("opponentRecentResults", opponentRecentResults);
	        
	        // 상대 전적
	        String myRecord = gameService.getHeadToHeadRecord(myTeamId, opponentId);
	        String opponentRecord = gameService.getHeadToHeadRecord(opponentId, myTeamId);

	        model.addAttribute("myRecord", myRecord);
	        model.addAttribute("opponentRecord", opponentRecord);
	    }
	    
	    // 주요 선수 추가
	    TopPlayerCardView hitter = gameService.getTopHitter(myTeamId, season);
        TopPlayerCardView pitcher = gameService.getTopPitcher(myTeamId, season);
        model.addAttribute("topHitter", hitter);
        model.addAttribute("topPitcher", pitcher);
	    
	    
	    // 팀 순위 카드뷰
	    List<TeamRankingCardView> rankingList = gameService.getTeamRankingCardViews(season);
	    model.addAttribute("rankingList", rankingList);

	    return "user/home";
	}
}
