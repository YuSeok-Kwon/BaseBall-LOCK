package com.kepg.BaseBallLOCK.team.teamRanking.teamRankingDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.kepg.BaseBallLOCK.team.teamRanking.teamRankingDto.TeamRankingDTO;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TeamRankingDAO {
	private final Connection conn;

	public void saveOrUpdate(TeamRankingDTO dto) {
	    String sql = "INSERT INTO teamRanking (teamId, season, ranking, games, wins, draws, losses, gamesBehind, winRate) " +
	                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
	                 "ON DUPLICATE KEY UPDATE " +
	                 "ranking = VALUES(ranking), " +
	                 "games = VALUES(games), " +
	                 "wins = VALUES(wins), " +
	                 "draws = VALUES(draws), " +
	                 "losses = VALUES(losses), " +
	                 "gamesBehind = VALUES(gamesBehind), " +
	                 "winRate = VALUES(winRate)";

	    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	        pstmt.setInt(1, dto.getTeamId());
	        pstmt.setInt(2, dto.getSeason());
	        pstmt.setInt(3, dto.getRanking());
	        pstmt.setInt(4, dto.getGames());
	        pstmt.setInt(5, dto.getWins());
	        pstmt.setInt(6, dto.getDraws());
	        pstmt.setInt(7, dto.getLosses());

	        if (dto.getGamesBehind() != null) {
	            pstmt.setDouble(8, dto.getGamesBehind());
	        } else {
	            pstmt.setNull(8, Types.DOUBLE);
	        }

	        pstmt.setDouble(9, dto.getWinRate());

	        pstmt.executeUpdate();

	    } catch (SQLException e) {
	        System.out.println("❌ 저장 실패: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
}

