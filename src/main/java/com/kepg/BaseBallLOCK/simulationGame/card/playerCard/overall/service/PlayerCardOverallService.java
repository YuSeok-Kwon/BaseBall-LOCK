package com.kepg.BaseBallLOCK.simulationGame.card.playerCard.overall.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kepg.BaseBallLOCK.player.stats.domain.BatterStats;
import com.kepg.BaseBallLOCK.player.stats.domain.PitcherStats;
import com.kepg.BaseBallLOCK.player.stats.repository.BatterStatsRepository;
import com.kepg.BaseBallLOCK.player.stats.repository.PitcherStatsRepository;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.dto.PlayerCardOverallDTO;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.overall.domain.PlayerCardOverall;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.overall.dto.RawBatterScore;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.overall.dto.RawPitcherScore;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.overall.repository.PlayerCardOverallRepository;
import com.kepg.BaseBallLOCK.simulationGame.card.playerCard.projection.PlayerCardOverallProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayerCardOverallService {
	
    private final BatterStatsRepository batterStatsRepository;
    private final PitcherStatsRepository pitcherStatsRepository;
    private final PlayerCardOverallRepository playerCardOverallRepository;
    
    // 타자 오버롤 계산 (포지션 + 시즌 리스트)
    public void calculateBatterOverallByPosition(List<Integer> seasons, String targetPosition) {

        List<RawBatterScore> rawList = collectFilteredRawScores(seasons, targetPosition);

        if (rawList.isEmpty()) return;

        Map<String, Double> minMap = new HashMap<>();
        Map<String, Double> maxMap = new HashMap<>();
        initMinMaxMaps(rawList, minMap, maxMap);

        for (RawBatterScore raw : rawList) {
            PlayerCardOverall entity = PlayerCardOverall.builder()
                .playerId(raw.getPlayerId())
                .season(raw.getSeason())
                .type("BATTER")
                .power(scaleToRange(raw.getPower(), minMap.get("power"), maxMap.get("power")))
                .contact(scaleToRange(raw.getContact(), minMap.get("contact"), maxMap.get("contact")))
                .discipline(scaleToRange(raw.getDiscipline(), minMap.get("discipline"), maxMap.get("discipline")))
                .speed(scaleToRange(raw.getSpeed(), minMap.get("speed"), maxMap.get("speed")))
                .control(0)
                .stuff(0)
                .stamina(0)
                .overall(scaleToRange(raw.getOverall(), minMap.get("overall"), maxMap.get("overall")))
                .build();

            playerCardOverallRepository.deleteByPlayerIdAndSeason(raw.getPlayerId(), raw.getSeason());
            playerCardOverallRepository.saveAndFlush(entity);
        }
    }
    
    // 타자 raw 데이터 필터링 (포지션, 시즌별로 조건 통과한 선수만)
    private List<RawBatterScore> collectFilteredRawScores(List<Integer> seasons, String targetPosition) {
        List<RawBatterScore> rawList = new ArrayList<>();

        for (int season : seasons) {
            List<BatterStats> allStats = batterStatsRepository.findBySeason(season);
            Map<Integer, Map<String, Double>> statMapByPlayer = new HashMap<>();
            Map<Integer, String> positionMap = new HashMap<>();

            for (BatterStats stat : allStats) {
                Integer playerId = stat.getPlayerId();
                statMapByPlayer.putIfAbsent(playerId, new HashMap<>());
                statMapByPlayer.get(playerId).put(stat.getCategory(), stat.getValue());
                positionMap.putIfAbsent(playerId, stat.getPosition());
            }

            for (Map.Entry<Integer, Map<String, Double>> entry : statMapByPlayer.entrySet()) {
                Integer playerId = entry.getKey();
                String position = positionMap.get(playerId);
                if (!targetPosition.equals(position)) continue;

                Map<String, Double> statMap = entry.getValue();
                double g = statMap.getOrDefault("G", 0.0);
                double pa = statMap.getOrDefault("PA", 0.0);
                if (g < 10 || pa < 20) continue;

                RawBatterScore raw = calculateRawBatterScoreWithWeight(playerId, season, statMap);
                raw.setSeason(season);
                rawList.add(raw);
            }
        }

        return rawList;
    }
    
    // 타자 점수 계산 (보정 포함)
    private RawBatterScore calculateRawBatterScoreWithWeight(Integer playerId, int season, Map<String, Double> statMap) {
        double war = statMap.getOrDefault("WAR", 0.0);
        double weight = war > 0 ? 1.0 + Math.pow(war, 1.05) / 4.0 : 1.0;
        double pa = statMap.getOrDefault("PA", 0.0);
        double gameRatio = Math.min(pa / 400.0, 1.5);

        double hrScore = Math.min(statMap.getOrDefault("HR", 0.0) / 50.0, 1.5) * 100;
        double slgScore = Math.min(statMap.getOrDefault("SLG", 0.0) / 0.6, 1.5) * 100;
        double power = (hrScore * 0.6 + slgScore * 0.4) * weight * gameRatio;

        double hScore = Math.min(statMap.getOrDefault("H", 0.0) / 180.0, 1.5) * 100;
        double avgScore = Math.min(statMap.getOrDefault("AVG", 0.0) / 0.35, 1.5) * 100;
        double contact = (hScore * 0.5 + avgScore * 0.5) * weight * gameRatio;

        double bbScore = Math.min(statMap.getOrDefault("BB", 0.0) / 80.0, 1.5) * 100;
        double soScore = (1.0 - Math.min(statMap.getOrDefault("SO", 0.0) / 150.0, 1.0)) * 100;
        double discipline = (bbScore * 0.5 + soScore * 0.5) * weight * gameRatio;

        double sbScore = Math.min(statMap.getOrDefault("SB", 0.0) / 50.0, 1.5) * 100;
        double speed = sbScore * weight * gameRatio;

        double overall = power * 0.3 + contact * 0.3 + discipline * 0.2 + speed * 0.2;

        return RawBatterScore.builder()
            .playerId(playerId)
            .power(power)
            .contact(contact)
            .discipline(discipline)
            .speed(speed)
            .overall(overall)
            .build();
    }
    
    // 정규화용 최대값: 상위 4% 평균값 계산
    private double getTopPercentAverage(List<Double> values, double topPercent) {
        int count = Math.max(1, (int) (values.size() * topPercent));
        return values.stream()
            .sorted(Comparator.reverseOrder())  // 내림차순
            .limit(count)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(1.0);  // fallback
    }
    
    // 정규화용 min/max 계산
    private void initMinMaxMaps(List<RawBatterScore> rawList, Map<String, Double> minMap, Map<String, Double> maxMap) {

        List<Double> powers = new ArrayList<>();
        List<Double> contacts = new ArrayList<>();
        List<Double> disciplines = new ArrayList<>();
        List<Double> speeds = new ArrayList<>();
        List<Double> overalls = new ArrayList<>();

        for (RawBatterScore raw : rawList) {
            powers.add(raw.getPower());
            contacts.add(raw.getContact());
            disciplines.add(raw.getDiscipline());
            speeds.add(raw.getSpeed());
            overalls.add(raw.getOverall());
        }

        minMap.put("power", Collections.min(powers));
        maxMap.put("power", getTopPercentAverage(powers, 0.04));

        minMap.put("contact", Collections.min(contacts));
        maxMap.put("contact", getTopPercentAverage(contacts, 0.04));

        minMap.put("discipline", Collections.min(disciplines));
        maxMap.put("discipline", getTopPercentAverage(disciplines, 0.04));

        minMap.put("speed", Collections.min(speeds));
        maxMap.put("speed", getTopPercentAverage(speeds, 0.04));

        minMap.put("overall", Collections.min(overalls));
        maxMap.put("overall", getTopPercentAverage(overalls, 0.04));
    }

    // 점수 정규화 (10~100)
    private int scaleToRange(double value, double min, double max) {
        if (max == min) return 10; // 모두 같은 값일 때
        return 10 + (int) Math.round((value - min) / (max - min) * 90);
    }
    
    // 투수 오버롤 계산 (선발/구원 포함)
    @Transactional
    public void calculatePitcherOverall(List<Integer> seasons) {

        List<RawPitcherScore> rawList = new ArrayList<>();

        for (int season : seasons) {
            List<PitcherStats> allStats = pitcherStatsRepository.findBySeason(season);

            Map<Integer, Map<String, Double>> statMapByPlayer = new HashMap<>();

            for (PitcherStats stat : allStats) {
                Integer playerId = stat.getPlayerId();
                statMapByPlayer.putIfAbsent(playerId, new HashMap<>());
                statMapByPlayer.get(playerId).put(stat.getCategory(), stat.getValue());
            }

            for (Map.Entry<Integer, Map<String, Double>> entry : statMapByPlayer.entrySet()) {
                Integer playerId = entry.getKey();
                Map<String, Double> statMap = entry.getValue();

                double ip = statMap.getOrDefault("IP", 0.0);
                double gs = statMap.getOrDefault("GS", 0.0);
                double gr = statMap.getOrDefault("GR", 0.0);

                if ((gs + gr) < 20 && ip < 30.0) continue;

                RawPitcherScore raw = calculateRawPitcherScoreWithMinMax(playerId, season, statMap);
                raw.setSeason(season);
                rawList.add(raw);
            }
        }

        // 최고, 최저값 계산 + 상위 4% 평균을 max로
        Map<String, Double> minMap = new HashMap<>();
        Map<String, Double> maxMap = new HashMap<>();
        initPitcherMinMaxMaps(rawList, minMap, maxMap);

        // 정규화 및 저장
        for (RawPitcherScore raw : rawList) {
            int control = scaleToRange(raw.getControl(), minMap.get("control"), maxMap.get("control"));
            int stuff = scaleToRange(raw.getStuff(), minMap.get("stuff"), maxMap.get("stuff"));
            int stamina = scaleToRange(raw.getStamina(), minMap.get("stamina"), maxMap.get("stamina"));
            int overall = scaleToRange(raw.getOverall(), minMap.get("overall"), maxMap.get("overall"));

            PlayerCardOverall entity = PlayerCardOverall.builder()
                .playerId(raw.getPlayerId())
                .season(raw.getSeason())
                .type("PITCHER")
                .power(0)
                .contact(0)
                .discipline(0)
                .speed(0)
                .control(control)
                .stuff(stuff)
                .stamina(stamina)
                .overall(overall)
                .build();

            playerCardOverallRepository.deleteByPlayerIdAndSeason(raw.getPlayerId(), raw.getSeason());
            playerCardOverallRepository.saveAndFlush(entity);
        }
    }
    
 // 투수 실제 데이터 추출 -> 보정
    private RawPitcherScore calculateRawPitcherScoreWithMinMax(int playerId, int season, Map<String, Double> statMap) {
        double war = statMap.getOrDefault("WAR", 0.0);
        double weight = war > 0 ? 1.0 + Math.pow(war, 1.05) / 4.0 : 1.0;

        double g = statMap.getOrDefault("G", 0.0);
        double bb = statMap.getOrDefault("BB", 0.0);
        double whip = statMap.getOrDefault("WHIP", 0.0);
        double so = statMap.getOrDefault("SO", 0.0);
        double era = statMap.getOrDefault("ERA", 0.0);
        double ip = statMap.getOrDefault("IP", 0.0);

        boolean isReliever = g >= 40;
        double gameRatio = isReliever ? Math.min(g / 40.0, 1.5) : Math.min(ip / 144.0, 1.5);

        double bbScore = (1.0 - Math.min(bb / 4.0, 1.0)) * 100;
        double whipScore = (1.0 - Math.min(whip / 2.0, 1.0)) * 100;
        double control = (bbScore * 0.6 + whipScore * 0.4) * weight * gameRatio;

        double soScore = Math.min(so / 150.0, 1.5) * 100;
        double eraScore = (1.0 - Math.min(era / 6.0, 1.0)) * 100;
        double stuff = (soScore * 0.7 + eraScore * 0.3) * weight * gameRatio;

        double stamina = Math.min(ip / 144.0, 1.5) * 100 * weight;

        double overall = isReliever
            ? (control * 0.45 + stuff * 0.45 + stamina * 0.1)
            : (control * 0.35 + stuff * 0.35 + stamina * 0.3);

        return RawPitcherScore.builder()
            .playerId(playerId)
            .control(control)
            .stuff(stuff)
            .stamina(stamina)
            .overall(overall)
            .build();
    }
    
    // 투수 정규화 범위 설정
    private void initPitcherMinMaxMaps(List<RawPitcherScore> rawList, Map<String, Double> minMap, Map<String, Double> maxMap) {
        List<Double> controls = new ArrayList<>();
        List<Double> stuffs = new ArrayList<>();
        List<Double> staminas = new ArrayList<>();
        List<Double> overalls = new ArrayList<>();

        for (RawPitcherScore raw : rawList) {
            controls.add(raw.getControl());
            stuffs.add(raw.getStuff());
            staminas.add(raw.getStamina());
            overalls.add(raw.getOverall());
        }

        minMap.put("control", Collections.min(controls));
        maxMap.put("control", getTopPercentAverage(controls, 0.04));

        minMap.put("stuff", Collections.min(stuffs));
        maxMap.put("stuff", getTopPercentAverage(stuffs, 0.04));

        minMap.put("stamina", Collections.min(staminas));
        maxMap.put("stamina", getTopPercentAverage(staminas, 0.04));

        minMap.put("overall", Collections.min(overalls));
        maxMap.put("overall", getTopPercentAverage(overalls, 0.04));
    }
    
    // 타자 + 투수 오버롤 한 번에 실행 (2020~24)
    public void calculateAllBatterAndPitcher() {

        List<Integer> legacySeasons = List.of(2020, 2021, 2022, 2023, 2024);

        // 타자
        calculateBatterOverallByPosition(legacySeasons, "1B");
        calculateBatterOverallByPosition(legacySeasons, "2B");
        calculateBatterOverallByPosition(legacySeasons, "3B");
        calculateBatterOverallByPosition(legacySeasons, "SS");
        calculateBatterOverallByPosition(legacySeasons, "LF");
        calculateBatterOverallByPosition(legacySeasons, "CF");
        calculateBatterOverallByPosition(legacySeasons, "RF");
        calculateBatterOverallByPosition(legacySeasons, "DH");
        calculateBatterOverallByPosition(legacySeasons, "C");


        // 투수
        calculatePitcherOverall(legacySeasons);
    }
    
    // 타자 + 투수 오버롤 한 번에 실행 (2025)
    public void calculateOnly2025() {

        List<Integer> currentSeason = List.of(2025);

        String[] positions = {"1B", "2B", "3B", "SS", "LF", "CF", "RF", "DH", "C"};
        for (String pos : positions) {
            calculateBatterOverallByPosition(currentSeason, pos);
        }

        calculatePitcherOverall(currentSeason);
    }
    
    // 전체 선수 카드 등급 부여
    public void assignGradesForAll() {

        assignGradesByType("BATTER");
        assignGradesByType("PITCHER");
    }
    
    // (S-D) 등급 분류
    public void assignGradesByType(String type) {

        List<PlayerCardOverall> list = playerCardOverallRepository.findByType(type);
        list.sort(Comparator.comparing(PlayerCardOverall::getOverall).reversed());

        int total = list.size();
        int sCount = (int) Math.ceil(total * 0.02);
        int aCount = sCount + (int) Math.ceil(total * 0.1);
        int bCount = aCount + (int) Math.ceil(total * 0.20);
        int cCount = bCount + (int) Math.ceil(total * 0.40);

        for (int i = 0; i < total; i++) {
            PlayerCardOverall card = list.get(i);
            String grade;

            if (i < sCount) grade = "S";
            else if (i < aCount) grade = "A";
            else if (i < bCount) grade = "B";
            else if (i < cCount) grade = "C";
            else grade = "D";

            card.setGrade(grade);
            playerCardOverallRepository.save(card);
        }
    }
       
	 // 선수 카드 조회
    @Transactional(readOnly = true)
    public PlayerCardOverallDTO getCardByPlayerAndSeason(int playerId, int season) {



        Optional<PlayerCardOverallProjection> optional = playerCardOverallRepository.findByPlayerIdAndSeason(playerId, season);

        if (optional.isPresent()) {
            PlayerCardOverallProjection projection = optional.get();
            PlayerCardOverallDTO dto = new PlayerCardOverallDTO();

            dto.setPlayerName(projection.getPlayerName());
            dto.setPlayerId(projection.getPlayerId());
            dto.setSeason(projection.getSeason());
            dto.setType(projection.getType());
            dto.setGrade(projection.getGrade());
            dto.setPower(projection.getPower());
            dto.setContact(projection.getContact());
            dto.setDiscipline(projection.getDiscipline());
            dto.setSpeed(projection.getSpeed());
            dto.setControl(projection.getControl());
            dto.setStuff(projection.getStuff());
            dto.setStamina(projection.getStamina());
            dto.setOverall(projection.getOverall());

            return dto;
        }

        return null; // 혹은 throw new EntityNotFoundException(...);
    }
   
}
