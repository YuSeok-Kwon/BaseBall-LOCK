package com.kepg.BaseBallLOCK.game.schedule.service;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;
import com.kepg.BaseBallLOCK.game.schedule.dto.ScheduleCardView;
import com.kepg.BaseBallLOCK.game.schedule.repository.ScheduleRepository;
import com.kepg.BaseBallLOCK.team.domain.Team;
import com.kepg.BaseBallLOCK.team.service.TeamService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TeamService teamService;
    
    private String formatKoreanDate(LocalDateTime dateTime) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN);
        String dayOfWeek = getKoreanDayOfWeek(dateTime.getDayOfWeek());
        return dateTime.format(dateFormatter) + "(" + dayOfWeek + ")";
    }
    
    private String getKoreanDayOfWeek(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "월";
            case TUESDAY: return "화";
            case WEDNESDAY: return "수";
            case THURSDAY: return "목";
            case FRIDAY: return "금";
            case SATURDAY: return "토";
            case SUNDAY: return "일";
            default: return "";
        }
    }
    
    @Transactional
    public void saveOrUpdate(Schedule newSchedule) {
        Optional<Schedule> optional = scheduleRepository.findByMatchDateAndHomeTeamIdAndAwayTeamId(
                newSchedule.getMatchDate(),
                newSchedule.getHomeTeamId(),
                newSchedule.getAwayTeamId()
        );

        Schedule schedule;
        if (optional.isPresent()) {
            schedule = optional.get();
            schedule.setHomeTeamScore(newSchedule.getHomeTeamScore());
            schedule.setAwayTeamScore(newSchedule.getAwayTeamScore());
            schedule.setStadium(newSchedule.getStadium());
            schedule.setStatus(newSchedule.getStatus());
        } else {
            schedule = newSchedule;
        }

        scheduleRepository.save(schedule);
    }

    // 오늘 경기(전체 중 하나)
    public Schedule getTodaySchedule() {
    	LocalDate today = LocalDate.now();
        Timestamp start = Timestamp.valueOf(today.atStartOfDay());
        Timestamp end = Timestamp.valueOf(today.atTime(23, 59, 59));
        return scheduleRepository.findFirstByMatchDateBetween(start, end);
    }
    
    // 팀 기준 오늘 경기
    public Schedule getTodayScheduleByTeam(int teamId) {
    	LocalDate today = LocalDate.now();
        Timestamp start = Timestamp.valueOf(today.atStartOfDay());
        Timestamp end = Timestamp.valueOf(today.atTime(23, 59, 59));
        
        return scheduleRepository.findTodayScheduleByTeam(start, end, teamId);
    }
    
    // 최근 경기 결과 리스트
    public List<String> getRecentResults(int teamId) {
        Timestamp todayStart = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        List<Schedule> matches = scheduleRepository.findRecentSchedules(teamId, todayStart);
        List<String> results = new ArrayList<>();

        for (Schedule match : matches) {
            Integer myScore = null;
            Integer oppScore = null;

            if (match.getHomeTeamId() == teamId) {
                myScore = match.getHomeTeamScore();
                oppScore = match.getAwayTeamScore();
            } else if (match.getAwayTeamId() == teamId) {
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
        return results ;
    }
    
    // 맞대결 전적
    public String getHeadToHeadRecord(int myTeamId, int opponentTeamId) {
    	Timestamp todayStart = Timestamp.valueOf(LocalDate.now().atStartOfDay());
        List<Schedule> matches = scheduleRepository.findHeadToHeadMatches2025(myTeamId, opponentTeamId, todayStart);

        int wins = 0, losses = 0, draws = 0;

        for (Schedule match : matches) {
            Integer myScore = null;
            Integer oppScore = null;

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
    	
    	YearMonth yearMonth = YearMonth.of(year, month);
        Timestamp start = Timestamp.valueOf(yearMonth.atDay(1).atStartOfDay());
        Timestamp end = Timestamp.valueOf(yearMonth.atEndOfMonth().atTime(23, 59, 59));

        List<Schedule> schedules = scheduleRepository.findByMatchDateBetweenOrderByMatchDate(start, end);
        Map<LocalDate, List<ScheduleCardView>> grouped = new LinkedHashMap<>();

        for (Schedule schedule : schedules) {
            LocalDate date = schedule.getMatchDate().toLocalDateTime().toLocalDate();
            if (schedule.getMatchDate() == null) {
            	continue;
            }
            Team homeTeam = teamService.getTeamById(schedule.getHomeTeamId());
            Team awayTeam = teamService.getTeamById(schedule.getAwayTeamId());

            if (homeTeam == null || awayTeam == null) {
                continue;
            }

            ScheduleCardView view = ScheduleCardView.builder()
                .id(schedule.getId())
                .matchDate(formatKoreanDate(schedule.getMatchDate().toLocalDateTime()))
                .homeTeamName(homeTeam.getName())
                .awayTeamName(awayTeam.getName())
                .homeTeamLogo(homeTeam.getLogoName())
                .awayTeamLogo(awayTeam.getLogoName())
                .homeTeamScore(schedule.getHomeTeamScore())
                .awayTeamScore(schedule.getAwayTeamScore())
                .stadium(schedule.getStadium())
                .build();

            if (!grouped.containsKey(date)) {
                grouped.put(date, new ArrayList<>());
            }
            grouped.get(date).add(view);
        }

        return grouped;
    	
    }

    public ScheduleCardView getScheduleDetailById(int matchId) {
        Optional<Schedule> optional = scheduleRepository.findById(matchId);
        if (optional.isEmpty()) {
            return null;
        }

        Schedule s = optional.get();

        Team home = teamService.getTeamById(s.getHomeTeamId());
        Team away = teamService.getTeamById(s.getAwayTeamId());

        String homeTeamName = "홈";
        String homeTeamLogo = "logo";
        String awayTeamName = "원정";
        String awayTeamLogo = "logo";

        if (home != null) {
            homeTeamName = home.getName();
            homeTeamLogo = home.getLogoName();
        }

        if (away != null) {
            awayTeamName = away.getName();
            awayTeamLogo = away.getLogoName();
        }

        return ScheduleCardView.builder()
                .id(s.getId())
                .matchDate(formatKoreanDate(s.getMatchDate().toLocalDateTime()))
                .homeTeamName(homeTeamName)
                .homeTeamLogo(homeTeamLogo)
                .homeTeamScore(s.getHomeTeamScore())
                .awayTeamName(awayTeamName)
                .awayTeamLogo(awayTeamLogo)
                .awayTeamScore(s.getAwayTeamScore())
                .stadium(s.getStadium())
                .status(s.getStatus())
                .build();
    }

    public Integer findScheduleIdByMatchInfo(Timestamp matchDateTime, int homeTeamId, int awayTeamId) {
        return scheduleRepository.findIdByDateAndTeams(matchDateTime, homeTeamId, awayTeamId);
    }
}
