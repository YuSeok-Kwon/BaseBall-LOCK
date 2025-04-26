package com.kepg.BaseBallLOCK.game.schedule.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    Schedule findFirstByMatchDateBetween(Timestamp start, Timestamp end);

    @Query("SELECT s FROM Schedule s " +
           "WHERE s.matchDate BETWEEN :start AND :end " +
           "AND (s.homeTeamId = :teamId OR s.awayTeamId = :teamId)")
    Schedule findTodayScheduleByTeam(@Param("start") Timestamp start,
                                     @Param("end") Timestamp end,
                                     @Param("teamId") int teamId);

    @Query(value = """
        SELECT * FROM schedule
        WHERE matchDate < :now
          AND status = '종료'
          AND (homeTeamId = :teamId OR awayTeamId = :teamId)
        ORDER BY matchDate DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Schedule> findRecentSchedules(@Param("teamId") int teamId,
                                       @Param("now") Timestamp now);

    @Query(value = """
        SELECT * FROM schedule
        WHERE matchDate >= '2025-01-01'
          AND matchDate < :todayStart
          AND status = '종료'
          AND ((homeTeamId = :myTeamId AND awayTeamId = :opponentTeamId)
               OR (homeTeamId = :opponentTeamId AND awayTeamId = :myTeamId))
        """, nativeQuery = true)
    List<Schedule> findHeadToHeadMatches2025(@Param("myTeamId") int myTeamId,
                                             @Param("opponentTeamId") int opponentTeamId,
                                             @Param("todayStart") Timestamp todayStart);

    List<Schedule> findByMatchDateBetweenOrderByMatchDate(Timestamp start, Timestamp end);

    @Query("SELECT s.id FROM Schedule s WHERE s.matchDate = :matchDate AND s.homeTeamId = :homeTeamId AND s.awayTeamId = :awayTeamId")
    Integer findIdByDateAndTeams(@Param("matchDate") Timestamp matchDate,
                                 @Param("homeTeamId") int homeTeamId,
                                 @Param("awayTeamId") int awayTeamId);
    

    Optional<Schedule> findByMatchDateAndHomeTeamIdAndAwayTeamId(Timestamp matchDate,
                                                                 int homeTeamId,
                                                                 int awayTeamId);
    
    Optional<Schedule> findById(int id);
    
    @Query("SELECT s.id FROM Schedule s WHERE s.matchDate < :currentDate ORDER BY s.matchDate DESC LIMIT 1")
    Integer findPrevMatchId(@Param("currentDate") Timestamp currentDate);

    @Query("SELECT s.id FROM Schedule s WHERE s.matchDate > :currentDate ORDER BY s.matchDate ASC LIMIT 1")
    Integer findNextMatchId(@Param("currentDate") Timestamp currentDate);
}