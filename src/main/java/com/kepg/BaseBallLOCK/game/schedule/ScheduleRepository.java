package com.kepg.BaseBallLOCK.game.schedule;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
	Schedule findFirstByMatchDateBetween(LocalDateTime start, LocalDateTime end);
	
	@Query("SELECT s FROM Schedule s " +
		       "WHERE s.matchDate BETWEEN :start AND :end " +
		       "AND (s.homeTeamId = :teamId OR s.awayTeamId = :teamId)")
		Schedule findTodayScheduleByTeam(@Param("start") LocalDateTime start,
		                                 @Param("end") LocalDateTime end,
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
		                                   @Param("now") LocalDateTime now);
	
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
		                                                    @Param("todayStart") LocalDateTime todayStart);
}
