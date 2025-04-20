package com.kepg.BaseBallLOCK.game.schedule.scheduleDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.kepg.BaseBallLOCK.game.schedule.scheduleDto.ScheduleDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ScheduleDAO {
	
	private final Connection conn;
	
	// 경기 정보 삽입 또는 업데이트
	public void insertOrUpdateMatch(ScheduleDTO schedule) {
	    String selectSql = "SELECT id FROM schedule WHERE matchDate = ? AND homeTeamId = ? AND awayTeamId = ?";
	    String insertSql = "INSERT INTO schedule (matchDate, homeTeamId, awayTeamId, stadium, status, homeTeamScore, awayTeamScore) " +
	                       "VALUES (?, ?, ?, ?, ?, ?, ?)";
	    String updateSql = "UPDATE schedule SET stadium = ?, status = ?, homeTeamScore = ?, awayTeamScore = ? WHERE id = ?";

	    try (
	        PreparedStatement selectStmt = conn.prepareStatement(selectSql);
	    ) {
	        selectStmt.setTimestamp(1, schedule.getMatchDate());
	        selectStmt.setInt(2, schedule.getHomeTeamId());
	        selectStmt.setInt(3, schedule.getAwayTeamId());

	        ResultSet rs = selectStmt.executeQuery();

	        if (rs.next()) {
	            // 이미 존재하는 경우 → update
	            int existingId = rs.getInt("id");
	            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
	                updateStmt.setString(1, schedule.getStadium());
	                updateStmt.setString(2, schedule.getStatus());
	                updateStmt.setObject(3, schedule.getHomeTeamScore(), java.sql.Types.INTEGER);
	                updateStmt.setObject(4, schedule.getAwayTeamScore(), java.sql.Types.INTEGER);
	                updateStmt.setInt(5, existingId);
	                updateStmt.executeUpdate();
	            }
	        } else {
	            // 없는 경우 → insert
	            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
	                insertStmt.setTimestamp(1, schedule.getMatchDate());
	                insertStmt.setInt(2, schedule.getHomeTeamId());
	                insertStmt.setInt(3, schedule.getAwayTeamId());
	                insertStmt.setString(4, schedule.getStadium());
	                insertStmt.setString(5, schedule.getStatus());
	                insertStmt.setObject(6, schedule.getHomeTeamScore(), java.sql.Types.INTEGER);
	                insertStmt.setObject(7, schedule.getAwayTeamScore(), java.sql.Types.INTEGER);
	                insertStmt.executeUpdate();
	            }
	        }

	    } catch (SQLException e) {
	        System.out.println("insertOrUpdateMatch 실패: " + e.getMessage());
	        e.printStackTrace();
	    }
	}

	// 모든 경기 조회
	public List<ScheduleDTO> findAllMatches() {
	    List<ScheduleDTO> scheduleList = new ArrayList<>();
	    String sql = "SELECT * FROM schedule ORDER BY matchDate";

	    try (PreparedStatement pstmt = conn.prepareStatement(sql);
	         ResultSet rs = pstmt.executeQuery()) {

	        while (rs.next()) {
	            ScheduleDTO schedule = new ScheduleDTO();
	            schedule.setId(rs.getInt("id"));
	            schedule.setMatchDate(rs.getTimestamp("matchDate"));
	            schedule.setHomeTeamId(rs.getInt("homeTeamId"));
	            schedule.setAwayTeamId(rs.getInt("awayTeamId"));
	            schedule.setStadium(rs.getString("stadium"));
	            schedule.setStatus(rs.getString("status"));
	            schedule.setHomeTeamScore(rs.getObject("homeTeamScore", Integer.class));
	            schedule.setAwayTeamScore(rs.getObject("awayTeamScore", Integer.class));

	            scheduleList.add(schedule);
	        }
	    } catch (SQLException e) {
	        System.out.println("ScheduleDAO 조회 실패: " + e.getMessage());
	        e.printStackTrace();
	    }

	    return scheduleList;
	}
}

