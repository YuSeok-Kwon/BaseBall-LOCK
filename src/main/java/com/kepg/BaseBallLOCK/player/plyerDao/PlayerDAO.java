package com.kepg.BaseBallLOCK.player.plyerDao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import com.kepg.BaseBallLOCK.player.playerDto.PlayerDTO;

public class PlayerDAO {

    private final Connection conn;

    public PlayerDAO(Connection conn) {
        this.conn = conn;
    }
    
 // 이름 + 포지션 + 시즌 + 팀 기준으로 조회
    public Optional<Integer> findByNamePositionAndSeasonAndTeam(String name, String position, int season, int teamId) {
        try {
            String sql = "SELECT id FROM player WHERE name = ? AND position = ? AND season = ? AND teamId = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, position);
            pstmt.setInt(3, season);
            pstmt.setInt(4, teamId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt("id"));
            }
        } catch (SQLException e) {
            System.out.println("PlayerDAO 조회 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return Optional.empty();
    }

    // 없으면 insert, 있으면 id 반환
    public int findOrCreatePlayerIdBySeason(PlayerDTO player) {
        Optional<Integer> existing = findByNamePositionAndSeasonAndTeam(
            player.getName(),
            player.getPosition(),
            player.getSeason(),
            player.getTeamId()
        );
        if (existing.isPresent()) {
            return existing.get();
        } else {
            return insert(player);
        }
    }

        // INSERT
        public int insert(PlayerDTO player) {
            try {
                String sql = "INSERT INTO player (teamId, name, position, season) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, player.getTeamId());
                pstmt.setString(2, player.getName());
                pstmt.setString(3, player.getPosition());
                pstmt.setInt(4, player.getSeason());

                pstmt.executeUpdate();
                ResultSet keys = pstmt.getGeneratedKeys();
                if (keys.next()) {
                    return keys.getInt(1);
                }
            } catch (SQLException e) {
                System.out.println("PlayerDAO 저장 실패: " + e.getMessage());
                e.printStackTrace();
            }
            return -1;
        }
    
}
