package com.kepg.BaseBallLOCK.game.schedule.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kepg.BaseBallLOCK.game.lineUp.service.LineupService;
import com.kepg.BaseBallLOCK.game.record.dto.PitcherRecordDTO;
import com.kepg.BaseBallLOCK.game.record.service.RecordService;
import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;
import com.kepg.BaseBallLOCK.game.schedule.dto.GameDetailCardView;
import com.kepg.BaseBallLOCK.game.schedule.dto.ScheduleCardView;
import com.kepg.BaseBallLOCK.game.schedule.repository.ScheduleRepository;
import com.kepg.BaseBallLOCK.game.scoreBoard.domain.ScoreBoard;
import com.kepg.BaseBallLOCK.game.scoreBoard.service.ScoreBoardService;
import com.kepg.BaseBallLOCK.team.domain.Team;
import com.kepg.BaseBallLOCK.team.service.TeamService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TeamService teamService;
    private final ScoreBoardService scoreBoardService;
    private final LineupService lineupService;
    private final RecordService recordService;

    @Transactional
    public void saveOrUpdate(Schedule newSchedule) {
        Optional<Schedule> optional = scheduleRepository.findByMatchDateAndHomeTeamIdAndAwayTeamId(
                newSchedule.getMatchDate(),
                newSchedule.getHomeTeamId(),
                newSchedule.getAwayTeamId()
        );

        Schedule schedule = optional.orElse(newSchedule);
        schedule.setHomeTeamScore(newSchedule.getHomeTeamScore());
        schedule.setAwayTeamScore(newSchedule.getAwayTeamScore());
        schedule.setStadium(newSchedule.getStadium());
        schedule.setStatus(newSchedule.getStatus());

        scheduleRepository.save(schedule);
    }

    public Schedule getTodaySchedule() {
        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));
        return scheduleRepository.findFirstByMatchDateBetween(start, end);
    }

    public Schedule getTodayScheduleByTeam(int teamId) {
        Timestamp start = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        Timestamp end = Timestamp.valueOf(LocalDate.now().atTime(23, 59, 59));
        return scheduleRepository.findTodayScheduleByTeam(start, end, teamId);
    }

    public List<String> getRecentResults(int teamId) {
        Timestamp todayStart = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        List<Schedule> matches = scheduleRepository.findRecentSchedules(teamId, todayStart);
        List<String> results = new ArrayList<>();

        for (Schedule match : matches) {
            Integer myScore = null, oppScore = null;
            if (match.getHomeTeamId() == teamId) {
                myScore = match.getHomeTeamScore();
                oppScore = match.getAwayTeamScore();
            } else {
                myScore = match.getAwayTeamScore();
                oppScore = match.getHomeTeamScore();
            }

            if (myScore == null || oppScore == null) {
                results.add("무");
            } else if (myScore > oppScore) {
                results.add("승");
            } else if (myScore < oppScore) {
                results.add("패");
            } else {
                results.add("무");
            }
        }

        Collections.reverse(results);
        return results;
    }

    public String getHeadToHeadRecord(int myTeamId, int opponentTeamId) {
        Timestamp todayStart = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        List<Schedule> matches = scheduleRepository.findHeadToHeadMatches2025(myTeamId, opponentTeamId, todayStart);
        int wins = 0, losses = 0, draws = 0;

        for (Schedule match : matches) {
            Integer myScore, oppScore;
            if (match.getHomeTeamId() == myTeamId) {
                myScore = match.getHomeTeamScore();
                oppScore = match.getAwayTeamScore();
            } else {
                myScore = match.getAwayTeamScore();
                oppScore = match.getHomeTeamScore();
            }

            if (myScore == null || oppScore == null) continue;
            if (myScore > oppScore) wins++;
            else if (myScore < oppScore) losses++;
            else draws++;
        }

        return String.format("%d승 %d패 %d무", wins, losses, draws);
    }

    public Map<LocalDate, List<ScheduleCardView>> getGroupedScheduleByMonth(int year, int month) {
        Timestamp start = Timestamp.valueOf(YearMonth.of(year, month).atDay(1).atStartOfDay());
        Timestamp end = Timestamp.valueOf(YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59));
        List<Schedule> schedules = scheduleRepository.findByMatchDateBetweenOrderByMatchDate(start, end);

        Map<LocalDate, List<ScheduleCardView>> grouped = new LinkedHashMap<>();

        for (Schedule schedule : schedules) {
            LocalDate date = schedule.getMatchDate().toLocalDateTime().toLocalDate();
            Team homeTeam = teamService.getTeamById(schedule.getHomeTeamId());
            Team awayTeam = teamService.getTeamById(schedule.getAwayTeamId());
            if (homeTeam == null || awayTeam == null) continue;

            ScheduleCardView view = ScheduleCardView.builder()
                    .id(schedule.getId())
                    .matchDate(schedule.getMatchDate())
                    .homeTeamName(homeTeam.getName())
                    .awayTeamName(awayTeam.getName())
                    .homeTeamLogo(homeTeam.getLogoName())
                    .awayTeamLogo(awayTeam.getLogoName())
                    .homeTeamScore(schedule.getHomeTeamScore())
                    .awayTeamScore(schedule.getAwayTeamScore())
                    .stadium(schedule.getStadium())
                    .status(schedule.getStatus())
                    .build();

            grouped.computeIfAbsent(date, k -> new ArrayList<>()).add(view);
        }

        return grouped;
    }

    public List<ScheduleCardView> getSchedulesByDate(LocalDate date) {
        Timestamp start = Timestamp.valueOf(date.atStartOfDay());
        Timestamp end = Timestamp.valueOf(date.atTime(23, 59, 59));
        List<Schedule> schedules = scheduleRepository.findByMatchDateBetweenOrderByMatchDate(start, end);

        return schedules.stream().map(schedule -> {
            int homeTeamId = schedule.getHomeTeamId();
            int awayTeamId = schedule.getAwayTeamId();

            return ScheduleCardView.builder()
                    .id(schedule.getId())
                    .matchDate(schedule.getMatchDate())
                    .homeTeamName(teamService.getTeamNameById(homeTeamId))
                    .awayTeamName(teamService.getTeamNameById(awayTeamId))
                    .homeTeamLogo(teamService.getTeamLogoById(homeTeamId))
                    .awayTeamLogo(teamService.getTeamLogoById(awayTeamId))
                    .homeTeamScore(schedule.getHomeTeamScore())
                    .awayTeamScore(schedule.getAwayTeamScore())
                    .stadium(schedule.getStadium())
                    .status(schedule.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    public ScheduleCardView getScheduleDetailById(int matchId) {
        Optional<Schedule> optional = scheduleRepository.findById(matchId);
        if (optional.isEmpty()) return null;

        Schedule s = optional.get();

        return ScheduleCardView.builder()
                .id(s.getId())
                .matchDate(s.getMatchDate())
                .homeTeamName(teamService.getTeamNameById(s.getHomeTeamId()))
                .awayTeamName(teamService.getTeamNameById(s.getAwayTeamId()))
                .homeTeamLogo(teamService.getTeamLogoById(s.getHomeTeamId()))
                .awayTeamLogo(teamService.getTeamLogoById(s.getAwayTeamId()))
                .homeTeamScore(s.getHomeTeamScore())
                .awayTeamScore(s.getAwayTeamScore())
                .stadium(s.getStadium())
                .status(s.getStatus())
                .build();
    }

    public Integer findScheduleIdByMatchInfo(Timestamp matchDateTime, int homeTeamId, int awayTeamId) {
        return scheduleRepository.findIdByDateAndTeams(matchDateTime, homeTeamId, awayTeamId);
    }

    public GameDetailCardView getGameDetail(int matchId) {
        Optional<Schedule> optionalSchedule = scheduleRepository.findById(matchId);
        if (optionalSchedule.isEmpty()) return null;

        Schedule schedule = optionalSchedule.get();
        int homeTeamId = schedule.getHomeTeamId();
        int awayTeamId = schedule.getAwayTeamId();

        Team homeTeam = teamService.getTeamById(homeTeamId);
        Team awayTeam = teamService.getTeamById(awayTeamId);

        List<PitcherRecordDTO> allPitcherRecords = recordService.getAllPitcherRecordsByScheduleId(matchId);

        List<String> holdPitchers = new ArrayList<>();
        String savePitcher = null;

        for (PitcherRecordDTO record : allPitcherRecords) {
            if ("HLD".equals(record.getDecision())) {
                holdPitchers.add(record.getPlayerName());
            }
            if (savePitcher == null && "SV".equals(record.getDecision())) {
                savePitcher = record.getPlayerName();
            }
        }
        
        ScoreBoard scoreBoard = scoreBoardService.findByScheduleId(matchId);

        if (scoreBoard == null) {
            return GameDetailCardView.builder()
                .matchDate(schedule.getMatchDate())
                .stadium(schedule.getStadium())
                .homeTeamName(homeTeam.getName())
                .awayTeamName(awayTeam.getName())
                .homeTeamLogo(homeTeam.getLogoName())
                .awayTeamLogo(awayTeam.getLogoName())
                .homeScore(null)
                .awayScore(null)
                .status("취소")
                .homeTeamColor(homeTeam.getColor())
                .awayTeamColor(awayTeam.getColor())
                .build();
        }

        return GameDetailCardView.builder()
                .matchDate(schedule.getMatchDate())
                .stadium(schedule.getStadium())
                .homeTeamName(homeTeam.getName())
                .awayTeamName(awayTeam.getName())
                .homeTeamLogo(homeTeam.getLogoName())
                .awayTeamLogo(awayTeam.getLogoName())
                .homeScore(schedule.getHomeTeamScore())
                .awayScore(schedule.getAwayTeamScore())
                .homeInningScores(convertInningScores(scoreBoard.getHomeInningScores()))
                .awayInningScores(convertInningScores(scoreBoard.getAwayInningScores()))
                .homeHits(scoreBoard.getHomeH())
                .homeErrors(scoreBoard.getHomeE())
                .awayHits(scoreBoard.getAwayH())
                .awayErrors(scoreBoard.getAwayE())
                .homeBatterLineup(lineupService.getBatterLineup(matchId, homeTeamId))
                .awayBatterLineup(lineupService.getBatterLineup(matchId, awayTeamId))
                .homeBatterRecords(recordService.getBatterRecords(matchId, homeTeamId))
                .awayBatterRecords(recordService.getBatterRecords(matchId, awayTeamId))
                .homePitcherRecords(recordService.getPitcherRecords(matchId, homeTeamId))
                .awayPitcherRecords(recordService.getPitcherRecords(matchId, awayTeamId))
                .winPitcher(scoreBoard.getWinPitcher())
                .losePitcher(scoreBoard.getLosePitcher())
                .savePitcher(savePitcher)
                .holdPitchers(holdPitchers)
                .homeTeamColor(homeTeam.getColor())
                .awayTeamColor(awayTeam.getColor())
                .build();
    }

    private List<Integer> convertInningScores(String scoreString) {
        if (scoreString == null || scoreString.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(scoreString.split(" "))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
    
    public Integer getPrevMatchId(int currentMatchId) {
        Optional<Schedule> current = scheduleRepository.findById(currentMatchId);
        if (current.isEmpty()) return null;

        Timestamp currentDate = current.get().getMatchDate();
        return scheduleRepository.findPrevMatchId(currentDate);
    }

    public Integer getNextMatchId(int currentMatchId) {
        Optional<Schedule> current = scheduleRepository.findById(currentMatchId);
        if (current.isEmpty()) return null;

        Timestamp currentDate = current.get().getMatchDate();
        return scheduleRepository.findNextMatchId(currentDate);
    }
    
    public Integer findIdByDateAndTeams(Timestamp matchDateTime, int homeTeamId, int awayTeamId) {
        return scheduleRepository.findIdByDateAndTeams(matchDateTime, homeTeamId, awayTeamId);
    }
    
    public List<Schedule> findByMatchDateAndTeam(LocalDate matchDate, int teamId) {
        Timestamp start = Timestamp.valueOf(matchDate.atStartOfDay());
        Timestamp end = Timestamp.valueOf(matchDate.atTime(23, 59, 59));

        return scheduleRepository.findByMatchDateBetweenAndTeam(start, end, teamId);
    }
    
    public Map<Integer, Integer> getTeamGamesPlayedBySeason(int season) {
        List<Object[]> resultList = scheduleRepository.countGamesByTeam(season);
        Map<Integer, Integer> teamGamesMap = new HashMap<>();

        for (Object[] row : resultList) {
        	Integer teamId = ((Number) row[0]).intValue();
        	Integer gameCount = ((Number) row[1]).intValue();
            teamGamesMap.put(teamId, gameCount);
        }

        return teamGamesMap;
    }
    
    public Timestamp findMatchDateById(int scheduleId) {
    	return scheduleRepository.findMatchDateById(scheduleId);
    }
	
	

}