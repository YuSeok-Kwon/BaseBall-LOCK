package com.kepg.BaseBallLOCK.crawler;

import com.kepg.BaseBallLOCK.team.teamRankingDto.TeamRankingDTO;

import com.kepg.BaseBallLOCK.team.teamRankingDao.TeamRankingDAO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class StatizTeamRankingCrawler {

    public static void main(String[] args) {
        String baseUrl = "https://statiz.sporki.com/season/?m=teamoverall&year=%d";

        Map<String, Integer> teamNameToId = new HashMap<>();
        teamNameToId.put("KIA", 1);
        teamNameToId.put("두산", 2);
        teamNameToId.put("삼성", 3);
        teamNameToId.put("SSG", 4);
        teamNameToId.put("SK", 4);
        teamNameToId.put("LG", 5);
        teamNameToId.put("한화", 6);
        teamNameToId.put("NC", 7);
        teamNameToId.put("KT", 8);
        teamNameToId.put("롯데", 9);
        teamNameToId.put("키움", 10);

        int[] years = {2020, 2021, 2022, 2023, 2024};

        try (Connection conn = java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/BaseBallLOCK?serverTimezone=Asia/Seoul", "root", "rnjs7944")) {

            TeamRankingDAO teamRankingDAO = new TeamRankingDAO(conn);

            for (int year : years) {
                System.out.println("시즌 " + year + " 팀 Ranking 데이터수집 시작");
                String url = String.format(baseUrl, year);
                System.out.println("크롤링 대상 URL: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .referrer("https://www.google.com")
                        .timeout(10000)
                        .get();

                Elements tables = doc.select("div.item_box table");
                System.out.println("테이블 개수: " + tables.size());

                if (tables.size() < 1) {
                    System.out.println("정규시즌 중 순위 테이블이 없습니다.");
                    continue;
                }

                Element rankingTable = tables.get(1);
                Elements rows = rankingTable.select("tbody > tr");

                for (Element row : rows) {
                    Elements cols = row.select("td");
                    if (cols.size() < 8) continue;

                    String teamName = cols.get(1).text().trim();
                    int teamId = teamNameToId.getOrDefault(teamName, 0);
                    if (teamId == 0) continue;

                    int ranking = Integer.parseInt(cols.get(0).text().trim());
                    int games = Integer.parseInt(cols.get(2).text().trim());
                    int wins = Integer.parseInt(cols.get(3).text().trim());
                    int draws = Integer.parseInt(cols.get(4).text().trim());
                    int losses = Integer.parseInt(cols.get(5).text().trim());

                    String gbText = cols.get(6).text().trim();
                    double gamesBehind = gbText.equals("-") ? 0.0 : Double.parseDouble(gbText);

                    double winRate = Double.parseDouble(cols.get(7).text().trim());

                    TeamRankingDTO dto = new TeamRankingDTO();
                    dto.setSeason(year);
                    dto.setTeamId(teamId);
                    dto.setRanking(ranking);
                    dto.setGames(games);
                    dto.setWins(wins);
                    dto.setDraws(draws);
                    dto.setLosses(losses);
                    dto.setGamesBehind(gamesBehind);
                    dto.setWinRate(winRate);

                    teamRankingDAO.saveOrUpdate(dto);
                    System.out.printf("저장 완료 - 시즌 %d, 팀 %s (%d위)\n", year, teamName, ranking);
                }
            }

        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
