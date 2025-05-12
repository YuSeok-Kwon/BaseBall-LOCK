package com.kepg.BaseBallLOCK.game.schedule.repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {
	
	// 오늘 가장빠른 경기 일정 조회
    Schedule findFirstByMatchDateBetween(Timestamp start, Timestamp end);

	// 특정 팀의 오늘 경기 일정 조회 (start~end 사이)
    @Query(value = """
    	    SELECT * FROM schedule
    	    WHERE matchDate BETWEEN :start AND :end
    	      AND (homeTeamId = :teamId OR awayTeamId = :teamId)
    	    """, nativeQuery = true)
    Schedule findTodayScheduleByTeam(@Param("start") Timestamp start,
    	                                 @Param("end") Timestamp end,
    	                                 @Param("teamId") int teamId);
 
    // 특정 팀의 가장 최근 종료된 경기 5개 조회
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

    // 2025 시즌 기준 특정 팀과 상대 팀 간의 맞대결 경기 목록 조회
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

    // 특정 날짜 범위의 모든 일정 조회 (시간 순 정렬)
    List<Schedule> findByMatchDateBetweenOrderByMatchDate(Timestamp start, Timestamp end);

    // 경기 날짜와 양 팀 기준으로 scheduleId 조회
    @Query(value = """
    	    SELECT id FROM schedule
    	    WHERE matchDate = :matchDate
    	      AND homeTeamId = :homeTeamId
    	      AND awayTeamId = :awayTeamId
    	    """, nativeQuery = true)
    Integer findIdByDateAndTeams(@Param("matchDate") Timestamp matchDate,
    	                             @Param("homeTeamId") int homeTeamId,
    	                             @Param("awayTeamId") int awayTeamId);

    // 경기 날짜 + 양 팀 기준으로 경기 정보 조회
    @Query(value = "SELECT * FROM schedule WHERE DATE(matchDate) = DATE(:matchDate) AND homeTeamId = :homeTeamId AND awayTeamId = :awayTeamId", nativeQuery = true)
    Optional<Schedule> findByMatchDateAndHomeTeamIdAndAwayTeamId(
        @Param("matchDate") Timestamp matchDate,
        @Param("homeTeamId") int homeTeamId,
        @Param("awayTeamId") int awayTeamId
    );
    
    // scheduleId 기준으로 경기 정보 조회
    Optional<Schedule> findById(int id);
    
    // 기준 날짜 이전의 가장 최근 경기 id 조회 (1개)
	@Query(value = """
	    SELECT id FROM schedule
	    WHERE matchDate < :currentDate
	    ORDER BY matchDate DESC
	    LIMIT 1
	    """, nativeQuery = true)
	Integer findPrevMatchId(@Param("currentDate") Timestamp currentDate);

    // 기준 날짜 이후의 다음 경기 id 조회 (1개)
	@Query(value = """
	    SELECT id FROM schedule
	    WHERE matchDate > :currentDate
	    ORDER BY matchDate ASC
	    LIMIT 1
	    """, nativeQuery = true)
	Integer findNextMatchId(@Param("currentDate") Timestamp currentDate);    

	// 특정 날짜 구간 + 팀 기준으로 경기 목록 조회 (native)
	@Query(value = """
	    SELECT *
	    FROM schedule
	    WHERE matchDate BETWEEN :start AND :end
	      AND (homeTeamId = :teamId OR awayTeamId = :teamId)
	    """, nativeQuery = true)
	List<Schedule> findByMatchDateBetweenAndTeam(@Param("start") Timestamp start,
	                                              @Param("end") Timestamp end,
	                                              @Param("teamId") int teamId);
    
    // 시즌별 팀당 경기 수 조회 (홈/원정 포함, 종료 경기만)
    @Query(value = """
    	    SELECT teamId, COUNT(*) AS gamesPlayed
			FROM (
			    SELECT s.homeTeamId AS teamId
			    FROM schedule s
			    WHERE YEAR(s.matchDate) = :season
			      AND s.matchDate < CURRENT_DATE()
			      AND s.homeTeamScore IS NOT NULL
			      AND s.awayTeamScore IS NOT NULL
			    UNION ALL
			    SELECT s.awayTeamId AS teamId
			    FROM schedule s
			    WHERE YEAR(s.matchDate) = :season
			      AND s.matchDate < CURRENT_DATE()
			      AND s.homeTeamScore IS NOT NULL
			      AND s.awayTeamScore IS NOT NULL
			) AS all_teams
			GROUP BY teamId
    	""", nativeQuery = true)
    List<Object[]> countGamesByTeam(@Param("season") int season);

    // scheduleId로 경기 날짜만 조회 (native)
    @Query(value = """
        SELECT matchDate
        FROM schedule
        WHERE id = :scheduleId
        """, nativeQuery = true)
    Timestamp findMatchDateById(@Param("scheduleId") int scheduleId);
    
    @Query(value = "SELECT id FROM schedule WHERE statizId = :statizId", nativeQuery = true)
    Integer findIdByStatizId(@Param("statizId") int statizId);
}