package com.kepg.BaseBallLOCK.game;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.game.schedule.ScheduleRepository;
import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;
import com.kepg.BaseBallLOCK.player.PlayerRepository;
import com.kepg.BaseBallLOCK.player.playerDto.TopPlayerCardView;
import com.kepg.BaseBallLOCK.player.stats.StatsRepository;
import com.kepg.BaseBallLOCK.player.stats.domain.Stats;
import com.kepg.BaseBallLOCK.team.TeamRepository;
import com.kepg.BaseBallLOCK.team.teamDomain.Team;
import com.kepg.BaseBallLOCK.team.teamRanking.TeamRankingRepository;
import com.kepg.BaseBallLOCK.team.teamRanking.domain.TeamRanking;
import com.kepg.BaseBallLOCK.team.teamRanking.teamRankingDto.TeamRankingCardView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {

    private final TeamRankingRepository teamRankingRepository;
    private final TeamRepository teamRepository;
    private final ScheduleRepository scheduleRepository;
    private final PlayerRepository playerRepository;
    private final StatsRepository statsRepository;

    public List<TeamRanking> getTeamRankings(int season) {
        return teamRankingRepository.findBySeasonOrderByRankingAsc(season);
    }

    public Schedule getTodaySchedule() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);
        return scheduleRepository.findFirstByMatchDateBetween(start, end);
    }

    public List<TeamRankingCardView> getTeamRankingCardViews(int season) {
        List<TeamRanking> rankings = teamRankingRepository.findBySeasonOrderByRankingAsc(season);
        List<TeamRankingCardView> cardViewList = new ArrayList<>();

        for (TeamRanking ranking : rankings) {
            Team team = teamRepository.findById(ranking.getTeamId()).orElse(null);
            if (team == null) continue;

            TeamRankingCardView cardView = TeamRankingCardView.builder()
                    .ranking(ranking.getRanking())
                    .games(ranking.getGames())
                    .wins(ranking.getWins())
                    .losses(ranking.getLosses())
                    .draws(ranking.getDraws())
                    .winRate(ranking.getWinRate())
                    .gamesBehind(ranking.getGamesBehind())
                    .teamName(team.getName())
                    .logoName(team.getLogoName())
                    .build();

            cardViewList.add(cardView);
        }

        return cardViewList;
    }

    public Schedule getTodayScheduleByTeam(int teamId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);
        return scheduleRepository.findTodayScheduleByTeam(start, end, teamId);
    }

    public Team getTeamInfo(int teamId) {
        return teamRepository.findById(teamId).orElse(null);
    }

    public Team getTeamById(int id) {
        return teamRepository.findById(id).orElse(null);
    }

    public List<String> getRecentResults(int teamId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
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
        return results;
    }

    public String getHeadToHeadRecord(int myTeamId, int opponentTeamId) {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
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

    public TopPlayerCardView getTopHitter(int teamId, int season) {
        List<Object[]> results = playerRepository.findTopHitterByTeamIdAndSeason(teamId, season);

        if (results == null || results.isEmpty()) {
            return null;
        }

        Object[] result = results.get(0);
        if (result.length < 5) return null;

        TopPlayerCardView view = TopPlayerCardView.builder()
            .name((String) result[0])
            .position((String) result[1])
            .war((Double) result[2])
            .warRank(result[3] != null ? (Integer) result[3] : 0)
            .build();

        Integer playerId = (Integer) result[4];
        enrichHitterStats(view, playerId);
        return view;
    }

    public TopPlayerCardView getTopPitcher(int teamId, int season) {
        List<Object[]> results = playerRepository.findTopPitcherByTeamIdAndSeason(teamId, season);

        if (results == null || results.isEmpty()) {
            return null;
        }

        Object[] result = results.get(0);
        if (result.length < 5) return null;

        TopPlayerCardView view = TopPlayerCardView.builder()
            .name((String) result[0])
            .position((String) result[1])
            .war((Double) result[2])
            .warRank(result[3] != null ? (Integer) result[3] : 0)
            .build();

        Integer playerId = (Integer) result[4];
        enrichPitcherStats(view, playerId);
        return view;
    }
    private void enrichHitterStats(TopPlayerCardView view, int playerId) {
        List<Stats> stats = statsRepository.findKeyStatsByPlayerId(playerId);
        Map<String, Stats> statMap = new HashMap<>();
        for (Stats stat : stats) {
            statMap.put(stat.getCategory(), stat);
        }

        Stats avg = statMap.get("AVG");
        Stats hr = statMap.get("HR");
        Stats ops = statMap.get("OPS");

        if (avg != null) view.setAvg(avg.getValue());
        if (hr != null) view.setHr(hr.getValue().intValue());
        if (ops != null) view.setOps(ops.getValue());
    }

    private void enrichPitcherStats(TopPlayerCardView view, int playerId) {
        List<Stats> stats = statsRepository.findKeyStatsByPlayerId(playerId);
        Map<String, Stats> statMap = new HashMap<>();
        for (Stats stat : stats) {
            statMap.put(stat.getCategory(), stat);
        }

        Stats era = statMap.get("ERA");
        Stats whip = statMap.get("WHIP");
        if (era != null) view.setEra(era.getValue());
        if (whip != null) view.setWhip(whip.getValue());

        Stats winsStat = statMap.get("W");
        Stats savesStat = statMap.get("SV");
        Stats holdsStat = statMap.get("HOLD");

        int wins = 0;
        int saves = 0;
        int holds = 0;

        if (winsStat != null) {
        	wins = winsStat.getValue().intValue();
        }
        if (savesStat != null) {
        	saves = savesStat.getValue().intValue();        
        }
        if (holdsStat != null) {
        	holds = holdsStat.getValue().intValue();        
        }

        if (wins >= saves && wins >= holds) {
            view.setBestStatLabel("WINS");
            view.setBestStatValue(wins);
        } else {
            if (saves >= wins && saves >= holds) {
                view.setBestStatLabel("SAVES");
                view.setBestStatValue(saves);
            } else {
                view.setBestStatLabel("HOLDS");
                view.setBestStatValue(holds);
            }
        }
    }
    
    
}

