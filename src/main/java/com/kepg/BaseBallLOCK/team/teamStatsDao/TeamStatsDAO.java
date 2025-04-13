package com.kepg.BaseBallLOCK.team.teamStatsDao;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TeamStatsDAO {
	private final Connection conn;

    public void insertStat(int teamId, int season, String category, double value, Integer rank) {
        try {
            String sql = "INSERT INTO teamStats (teamId, season, category, value, ranking) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, teamId);
            pstmt.setInt(2, season);
            pstmt.setString(3, category);
            pstmt.setDouble(4, value);
            if (rank != null) {
                pstmt.setInt(5, rank);
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("TeamDAO 저장 실패: " + e.getMessage());
        }
    }


	public void insertOrUpdateStat(int teamId, int season, String category, double value, Integer rank) {
	    String sql = "INSERT INTO teamStats (teamId, season, category, value) " +
	                 "VALUES (?, ?, ?, ?) " +
	                 "ON DUPLICATE KEY UPDATE value = VALUES(value)";
	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, teamId);
	        pstmt.setInt(2, season);
	        pstmt.setString(3, category);
	        pstmt.setDouble(4, value);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        System.out.println("TeamStatsDAO 저장 실패: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
}