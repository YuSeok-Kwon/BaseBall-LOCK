package com.kepg.BaseBallLOCK.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.kepg.BaseBallLOCK.team.teamStats.teamStatsDao.TeamStatsDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

public class StatizTeamWaaCrawler {

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/BaseBallLOCK?serverTimezone=Asia/Seoul";
        String username = "root";
        String password = "rnjs7944";

        Map<String, Integer> teamNameToId = new HashMap<>();
        teamNameToId.put("KIA", 1);
        teamNameToId.put("두산", 2);
        teamNameToId.put("삼성", 3);
        teamNameToId.put("SSG", 4);
        teamNameToId.put("LG", 5);
        teamNameToId.put("한화", 6);
        teamNameToId.put("NC", 7);
        teamNameToId.put("KT", 8);
        teamNameToId.put("롯데", 9);
        teamNameToId.put("키움", 10);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            TeamStatsDAO teamStatsDAO = new TeamStatsDAO(conn);

            int[] years = {2020, 2021, 2022, 2023};

            for (int year : years) {
                String url = String.format("https://statiz.sporki.com/season/?m=teamoverall&year=%d", year);
                System.out.println("\n시즌 " + year + " 팀 WAA 데이터 수집 시작");

                try {
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0")
                            .referrer("https://www.google.com")
                            .timeout(10000)
                            .get();

                    Elements tables = doc.select("div.item_box table");
                    if (tables.size() < 2) {
                        System.out.println("팀 분석(WAA) 테이블 없음 (year: " + year + ")");
                        continue;
                    }

                    Element waaTable = tables.get(3);
                    Elements rows = waaTable.select("tbody > tr");

                    for (Element row : rows) {
                        Elements cols = row.select("td");
                        if (cols.size() < 6) continue;
                        
                        
                        String teamName = cols.get(0).text().trim();
                        int teamId = teamNameToId.getOrDefault(teamName, 0);
                        if (teamId == 0) continue;

                        double hitting = Double.parseDouble(cols.get(1).text());
                        double running = Double.parseDouble(cols.get(2).text());
                        double defense = Double.parseDouble(cols.get(3).text());
                        double starting = Double.parseDouble(cols.get(4).text());
                        double bullpen = Double.parseDouble(cols.get(5).text());

                        teamStatsDAO.insertOrUpdateStat(teamId, year, "타격", hitting, null);
                        teamStatsDAO.insertOrUpdateStat(teamId, year, "주루", running, null);
                        teamStatsDAO.insertOrUpdateStat(teamId, year, "수비", defense, null);
                        teamStatsDAO.insertOrUpdateStat(teamId, year, "선발", starting, null);
                        teamStatsDAO.insertOrUpdateStat(teamId, year, "불펜", bullpen, null);

                        System.out.printf("저장 완료 - [%s] (%d)\n", teamName, year);
                    }

                    Thread.sleep((int) (1000 + Math.random() * 2000));

                } catch (Exception e) {
                    System.out.println("에러 발생 (year: " + year + "): " + e.getMessage());
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            System.out.println("DB 연결 실패: " + e.getMessage());
        }
    }
}
