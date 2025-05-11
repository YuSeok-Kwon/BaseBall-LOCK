package com.kepg.BaseBallLOCK.simulationGame.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.player.stats.repository.BatterStatsRepository;
import com.kepg.BaseBallLOCK.player.stats.repository.PitcherStatsRepository;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.overall.repository.PlayerCardOverallRepository;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.projection.PlayerCardOverallProjection;
import com.kepg.BaseBallLOCK.simulationGame.dto.GameReadyCardView;
import com.kepg.BaseBallLOCK.simulationGame.simulation.domain.SimulationGameSchedule;
import com.kepg.BaseBallLOCK.simulationGame.simulation.repository.SimulationGameScheduleRepository;
import com.kepg.BaseBallLOCK.simulationGame.userLineup.domain.UserLineup;
import com.kepg.BaseBallLOCK.simulationGame.userLineup.dto.UserLineupDTO;
import com.kepg.BaseBallLOCK.simulationGame.userLineup.repository.UserLineupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimulationGameService {

    private final PlayerCardOverallRepository playerCardOverallRepository;
    private final BatterStatsRepository batterStatsRepository;
    private final PitcherStatsRepository pitcherStatsRepository;
    private final UserLineupRepository userLineupRepository;
	private final SimulationGameScheduleRepository simulationGameScheduleRepository;


    // 봇 라인업 생성
    public List<PlayerCardOverallDTO> generateBotLineupWithStats(String difficulty) {

        int minOverall, maxOverall;

        switch (difficulty.toLowerCase()) {
            case "hard" -> {
                minOverall = 80;
                maxOverall = 140;
            }
            case "normal" -> {
                minOverall = 40;
                maxOverall = 80;
            }
            default -> {
                minOverall = 20;
                maxOverall = 50;
            }
        }

        Random random = new Random();
        List<String> basePositions = List.of("1B", "2B", "3B", "SS", "LF", "CF", "RF", "C");
        String duplicate = basePositions.get(random.nextInt(basePositions.size()));

        List<String> positions = new ArrayList<>(basePositions);
        positions.add(duplicate); // DH 포지션
        Collections.shuffle(positions);
        positions.add("P"); // 투수 추가

        List<PlayerCardOverallDTO> result = new ArrayList<>();

        for (String pos : positions) {
            PlayerCardOverallProjection projection = pos.equals("P")
                ? playerCardOverallRepository.findRandomPitcherByOverallRange(minOverall, maxOverall)
                : playerCardOverallRepository.findRandomByPositionAndOverallRange(pos, minOverall, maxOverall);

            if (projection != null) {
                PlayerCardOverallDTO card = convertToDTO(projection);
                card.setSeason(projection.getSeason());
                Integer playerId = card.getPlayerId();

                Map<String, Double> statMap;
                if ("PITCHER".equals(card.getType())) {
                    card.setPosition("P");
                    List<Object[]> raw = pitcherStatsRepository.findStatsRawByPlayerIdAndSeason(playerId, projection.getSeason());
                    statMap = convertToStatMap(raw);
                    card.setWar(statMap.getOrDefault("WAR", 0.0));
                    card.setEra(statMap.getOrDefault("ERA", 0.0));
                    card.setWhip(statMap.getOrDefault("WHIP", 0.0));
                    card.setWins(statMap.getOrDefault("W", 0.0).intValue());
                    card.setSaves(statMap.getOrDefault("SV", 0.0).intValue());
                    card.setHolds(statMap.getOrDefault("HLD", 0.0).intValue());
                } else {
                    List<Object[]> raw = batterStatsRepository.findStatsRawByPlayerIdAndSeason(playerId, projection.getSeason());
                    statMap = convertToStatMap(raw);
                    card.setWar(statMap.getOrDefault("WAR", 0.0));
                    card.setAvg(statMap.getOrDefault("AVG", 0.0));
                    card.setHr(statMap.getOrDefault("HR", 0.0).intValue());
                    card.setOps(statMap.getOrDefault("OPS", 0.0));
                    card.setSb(statMap.getOrDefault("SB", 0.0).intValue());
                }

                result.add(card);
            }
        }

        this.lastBotLineup = result;
        return result;
    }

    // 유저 라인업 조회
    public List<UserLineupDTO> getUserLineup(Integer userId) {

        List<UserLineup> entities = userLineupRepository.findByUserId(userId);
        List<UserLineupDTO> dtos = new ArrayList<>();
        for (UserLineup ul : entities) {
            UserLineupDTO dto = UserLineupDTO.builder()
                .playerId(ul.getPlayerId())
                .position(ul.getPosition())
                .orderNum(ul.getOrderNum())
                .season(ul.getSeason())
                .build();
            dtos.add(dto);
        }
        return dtos;
    }

    // Projection → DTO 변환
    private PlayerCardOverallDTO convertToDTO(PlayerCardOverallProjection p) {
        return PlayerCardOverallDTO.builder()
            .id(p.getId())
            .playerId(p.getPlayerId())
            .season(p.getSeason())
            .playerName(p.getPlayerName())
            .position(p.getPosition())
            .type(p.getType())
            .overall(p.getOverall())
            .grade(p.getGrade())
            .power(p.getPower())
            .contact(p.getContact())
            .discipline(p.getDiscipline())
            .speed(p.getSpeed())
            .control(p.getControl())
            .stuff(p.getStuff())
            .stamina(p.getStamina())
            .build();
    }

    
    // 유저 라인업 + 카드 정보 병합
    public List<GameReadyCardView> mergeCardInfo(List<UserLineupDTO> userLineup) {
        List<GameReadyCardView> result = new ArrayList<>();

        for (UserLineupDTO dto : userLineup) {
            Integer playerId = dto.getPlayerId();
            Integer season = dto.getSeason();

            Optional<PlayerCardOverallProjection> projection = playerCardOverallRepository
                .findByPlayerIdAndSeason(playerId, season);

            if (projection.isEmpty()) {
                System.out.println("PlayerCardOverallProjection 없음");
                continue;
            }

            PlayerCardOverallDTO card = convertToDTO(projection.get());

            Map<String, Double> statMap;
            if ("PITCHER".equals(card.getType())) {
                card.setPosition("P");
                List<Object[]> raw = pitcherStatsRepository.findStatsRawByPlayerIdAndSeason(playerId, season);
                statMap = convertToStatMap(raw);
                card.setWar(statMap.getOrDefault("WAR", 0.0));
                card.setEra(statMap.getOrDefault("ERA", 0.0));
                card.setWhip(statMap.getOrDefault("WHIP", 0.0));
                card.setWins(statMap.getOrDefault("W", 0.0).intValue());
                card.setSaves(statMap.getOrDefault("SV", 0.0).intValue());
                card.setHolds(statMap.getOrDefault("HLD", 0.0).intValue());
            } else {
                List<Object[]> raw = batterStatsRepository.findStatsRawByPlayerIdAndSeason(playerId, season);
                statMap = convertToStatMap(raw);
                card.setWar(statMap.getOrDefault("WAR", 0.0));
                card.setAvg(statMap.getOrDefault("AVG", 0.0));
                card.setHr(statMap.getOrDefault("HR", 0.0).intValue());
                card.setOps(statMap.getOrDefault("OPS", 0.0));
                card.setSb(statMap.getOrDefault("SB", 0.0).intValue());
            }

            GameReadyCardView view = GameReadyCardView.builder()
                .lineup(dto)
                .card(card)
                .build();

            result.add(view);
        }

        return result;
    }

    
    // Raw 통계 리스트(Object[]) → Map<String, Double> 형태로 변환
    private Map<String, Double> convertToStatMap(List<Object[]> rawStats) {
        Map<String, Double> statMap = new HashMap<>();
        for (Object[] row : rawStats) {
            if (row.length < 2) continue;
            String category = (String) row[0];
            Double value = (row[1] instanceof Number) ? ((Number) row[1]).doubleValue() : 0.0;
            statMap.put(category, value);
        }
        return statMap;
    }

    // 마지막 봇 라인업 저장
    private List<PlayerCardOverallDTO> lastBotLineup = new ArrayList<>();
    
    // 마지막으로 생성된 BOT 라인업 정보를 반환
    public List<PlayerCardOverallDTO> getLastBotLineup() {
        return lastBotLineup;
    }
    
    public int createSimulationSchedule(int userId, String difficulty) {
        SimulationGameSchedule schedule = new SimulationGameSchedule();
        schedule.setUserId(userId);
        schedule.setDifficulty(difficulty);
        simulationGameScheduleRepository.save(schedule);
        return schedule.getId(); // auto_increment 값
    }

}