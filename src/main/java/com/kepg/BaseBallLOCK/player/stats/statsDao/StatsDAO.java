package com.kepg.BaseBallLOCK.player.stats.statsDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatsDAO {
	 private final Connection conn;

    public void insertStat(int playerId, int season, String category, double value, Integer rank) {
        try {
            String sql = "INSERT INTO stats (playerId, season, category, value, ranking) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, playerId);
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
            System.out.println("PlayerStatsDAO 저장 실패: " + e.getMessage());
        }
    }
    
    public void insertOrUpdateStat(int playerId, int season, String category, double value, Integer rank) {
        String sql = "INSERT INTO stats (playerId, season, category, value, ranking) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE value = VALUES(value), ranking = VALUES(ranking)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, playerId);
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
            System.out.println("StatsDAO 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
