package com.kepg.BaseBallLOCK.simulationGame.simulation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;
import com.kepg.BaseBallLOCK.simulationGame.service.SimulationGameService;
import com.kepg.BaseBallLOCK.simulationGame.simulation.dto.InningOutcome;
import com.kepg.BaseBallLOCK.simulationGame.simulation.dto.InningResultDTO;
import com.kepg.BaseBallLOCK.simulationGame.simulation.dto.SimulationResultDTO;
import com.kepg.BaseBallLOCK.simulationGame.simulation.util.baseState.BaseState;
import com.kepg.BaseBallLOCK.simulationGame.userLineup.service.UserLineupService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimulationService {
	
	private final UserLineupService userLineupService;
	private final SimulationGameService simulationGameService;

    // 전체 경기 시뮬레이션 메서드
	public SimulationResultDTO playSimulationGame(List<PlayerCardOverallDTO> userLineup, List<PlayerCardOverallDTO> botLineup, String difficulty) {
	    SimulationResultDTO result = new SimulationResultDTO();
	    int userIdx = 0;
	    int botIdx = 0;

	    for (int inning = 1; inning <= 9; inning++) {
	        InningResultDTO inningResult = new InningResultDTO();
	        inningResult.setInningNumber(inning);

	        InningOutcome userOutcome = simulateHalfInning(userLineup, botLineup.get(9), userIdx, difficulty);
	        inningResult.setUserPlays(userOutcome.getPlays());
	        inningResult.setUserScore(userOutcome.getScore());
	        userIdx = userOutcome.getNextBatterIndex();

	        InningOutcome botOutcome = simulateHalfInning(botLineup, userLineup.get(9), botIdx, difficulty);
	        inningResult.setBotPlays(botOutcome.getPlays());
	        inningResult.setBotScore(botOutcome.getScore());
	        botIdx = botOutcome.getNextBatterIndex();

	        result.addInning(inningResult);

	        // 로그 누적
	        for (String log : userOutcome.getPlays()) {
	            result.getGameLog().add((inning) + "회초 🧑: " + log);
	        }
	        for (String log : botOutcome.getPlays()) {
	            result.getGameLog().add((inning) + "회말 🤖: " + log);
	        }
	        result.getGameLog().add(inning + "회 종료 점수 🧑 " + inningResult.getUserScore() + " : 🤖 " + inningResult.getBotScore());
	    }

	    return result;
	}

    // 이닝 단위 시뮬레이션
    private InningOutcome simulateHalfInning(List<PlayerCardOverallDTO> batters, PlayerCardOverallDTO pitcher, int startIndex, String difficulty) {

        List<String> plays = new ArrayList<>();
        int score = 0;
        int outs = 0;
        int batterIndex = startIndex;
        BaseState base = new BaseState();

        while (outs < 3) {
            PlayerCardOverallDTO batter = batters.get(batterIndex % 9);
            String outcome = playAtBat(batter, pitcher, difficulty);

            int orderNum = batterIndex % 9 + 1;

            if (isOut(outcome)) {
                if ("아웃".equals(outcome) && isDoublePlay(outcome, outs, base)) {
                    plays.add(orderNum + "번 타자: 병살");
                    outs += 2;
                } else {
                    plays.add(orderNum + "번 타자: " + outcome);
                    outs++;
                }
            } else {
                plays.add(orderNum + "번 타자: " + outcome);
                score += base.advanceBases(outcome);
            }

            batterIndex++;
        }

        InningOutcome result = new InningOutcome();
        result.setPlays(plays);
        result.setScore(score);
        result.setNextBatterIndex(batterIndex % 9);
        return result;
    }

    // 한 타석 결과 시뮬레이션
    public String playAtBat(PlayerCardOverallDTO batter, PlayerCardOverallDTO pitcher, String difficulty) {



        Random rand = new Random();

        double bbProb = calculateWalkProbability(batter, pitcher);
        double kProb = calculateStrikeoutProbability(batter, pitcher, difficulty);
        double hrProb = calculateHomerunProbability(batter, pitcher, difficulty);
        double doubleProb = calculateDoubleProbability(batter, difficulty);
        double hitProb = calculateHitProbability(batter, pitcher);

        double r = rand.nextDouble();

        if (r < bbProb) {
            return "볼넷";
        } else if (r < bbProb + kProb) {
            return "삼진";
        } else if (r < bbProb + kProb + hrProb) {
            return "홈런";
        } else if (r < bbProb + kProb + hrProb + doubleProb) {
            // 장타를 분기 처리
            double longHit = rand.nextDouble();
            if (longHit < 0.9) {
                return "2루타";
            } else {
                return "3루타";
            }
        } else if (r < bbProb + kProb + hrProb + doubleProb + hitProb) {
            return "안타";
        } else {
            return "아웃";
        }
    }

    // 병살 여부 판단
    public boolean isDoublePlay(String outcome, int outs, BaseState base) {
        if (!"아웃".equals(outcome)) return false;
        if (outs >= 2) return false;

        // 1루 주자 있고, 출루 방식이 안타 또는 볼넷일 때만 병살 가능
        return base.firstBase && (
            "안타".equals(base.firstBaseType) || 
            "볼넷".equals(base.firstBaseType)
        );
    }

    // 볼넷 확률 계산
    private double calculateWalkProbability(PlayerCardOverallDTO batter, PlayerCardOverallDTO pitcher) {
        double value = (batter.getDiscipline() * 0.6 - pitcher.getControl() * 0.5) / 100.0;
        return clamp(value, 0.05, 0.2);
    }

    // 삼진 확률 계산
    private double calculateStrikeoutProbability(PlayerCardOverallDTO batter, PlayerCardOverallDTO pitcher, String difficulty) {

        double base = (pitcher.getControl() * 0.6 - batter.getDiscipline() * 0.4) / 100.0;

        if ("hard".equalsIgnoreCase(difficulty)) {
            base += 0.03; // 봇이 삼진 잡기 쉬워짐
        }

        return clamp(base, 0.05, 0.3);
    }

    // 홈런 확률 계산
    private double calculateHomerunProbability(PlayerCardOverallDTO batter, PlayerCardOverallDTO pitcher, String difficulty) {

        double base = (batter.getPower() * 0.4 - pitcher.getStuff() * 0.3) / 100.0;

        if ("hard".equalsIgnoreCase(difficulty)) {
            base -= 0.01;
        }

        return clamp(base, 0.01, 0.05);
    }

    // 장타 확률 계산
    private double calculateDoubleProbability(PlayerCardOverallDTO batter, String difficulty) {

        double base = (batter.getContact() * 0.25 + batter.getSpeed() * 0.25) / 100.0;

        if ("hard".equalsIgnoreCase(difficulty)) {
            base -= 0.03;
        }

        return clamp(base, 0.02, 0.07);
    }

    // 안타 확률 계산
    private double calculateHitProbability(PlayerCardOverallDTO batter, PlayerCardOverallDTO pitcher) {

        double value = (batter.getContact() * 0.5 + batter.getSpeed() * 0.2 - pitcher.getStuff() * 0.4) / 100.0;
        return clamp(value, 0.09, 0.2);
    }
    
    // 아웃 여부
    private boolean isOut(String result) {
        return "아웃".equals(result) || "삼진".equals(result);
    }
    
    // 확률값 범위 제한
    private double clamp(double value, double min, double max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
    
    // 경기 전체 진행
    public List<InningResultDTO> playGame(Integer userId, String difficulty) {

        // 유저 라인업 + 투수 추출
        List<PlayerCardOverallDTO> userLineup = userLineupService.getSavedLineup(userId);
        PlayerCardOverallDTO userPitcher = findPitcherFromLineup(userLineup);

        // 봇 라인업 + 투수 추출
        List<PlayerCardOverallDTO> botLineup = simulationGameService.generateBotLineupWithStats(difficulty);
        PlayerCardOverallDTO pitcher = findPitcherFromLineup(botLineup);

        List<InningResultDTO> results = new ArrayList<>();

        int userStartIndex = 0;
        int botStartIndex = 0;

        // 1~9이닝 시뮬레이션
        for (int inning = 1; inning <= 9; inning++) {

            InningOutcome userOutcome = simulateHalfInning(userLineup, pitcher, userStartIndex, difficulty);
            InningOutcome botOutcome = simulateHalfInning(botLineup, userPitcher, botStartIndex, difficulty);

            userStartIndex = userOutcome.getNextBatterIndex();
            botStartIndex = botOutcome.getNextBatterIndex();

            InningResultDTO dto = new InningResultDTO();
            dto.setInningNumber(inning);
            dto.setUserPlays(userOutcome.getPlays());
            dto.setUserScore(userOutcome.getScore());
            dto.setBotPlays(botOutcome.getPlays());
            dto.setBotScore(botOutcome.getScore());

            results.add(dto);
        }

        return results;
    }
    
    // 라인업에서 투수 정보 가져오기
    private PlayerCardOverallDTO findPitcherFromLineup(List<PlayerCardOverallDTO> lineup) {
        for (PlayerCardOverallDTO card : lineup) {
            if ("P".equals(card.getPosition())) {
                return card;
            }
        }
        return null; // or throw new IllegalStateException("투수가 없습니다");
    }
    



}
