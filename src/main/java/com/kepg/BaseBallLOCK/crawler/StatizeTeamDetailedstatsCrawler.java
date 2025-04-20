package com.kepg.BaseBallLOCK.crawler;

import java.sql.Connection;

import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.kepg.BaseBallLOCK.team.teamStats.teamStatsDao.TeamStatsDAO;

public class StatizeTeamDetailedstatsCrawler {
	
    public static void main(String[] args) {
    	
        String jdbcUrl = "jdbc:mysql://localhost:3306/BaseBallLOCK?serverTimezone=Asia/Seoul";
        String username = "root";
        String password = "rnjs7944";

        String baseUrl = "https://statiz.sporki.com/stats/?m=team&m2=%s&m3=default&so=WAR&ob=DESC&year=%d"
                + "&sy=&ey=&te=&po=&lt=10100&reg=&pe=&ds=&de=&we=&hr=&ha=&ct=&st=&vp=&bo=&pt=&pp=&ii=&vc=&um=&oo="
                + "&rr=&sc=&bc=&ba=&li=&as=&ae=&pl=&gc=&lr=&pr=50&ph=&hs=&us=&na=&ls=&sf1=&sk1=&sv1=&sf2=&sk2=&sv2=";

        String[] types = {"batting", "pitching"};
        int[] years = {2020, 2021, 2022, 2023, 2024};

        // 팀 이름을 teamId로 매핑
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

            for (String type : types) {
                for (int year : years) {
                    String url = String.format(baseUrl, type, year);
                    System.out.println(year + "년 팀 " + type.toUpperCase());

                    try {
                        Document doc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0")
                                .referrer("https://www.google.com")
                                .timeout(10000)
                                .get();

                        Elements tables = doc.select("div.table_type01 > table");
                        if (tables == null || tables.size() < 2) continue;
                        Element table = tables.get(1);

                        Elements rows = table.select("tbody > tr");
                        for (Element row : rows) {
                            Elements cols = row.select("td");
                            if (type.equals("batting") && cols.size() >= 33) {
                                String teamName = cols.get(1).text();
                                String season = "20" + cols.get(2).text();
                                int teamId = teamNameToId.getOrDefault(teamName, 0);
                                if (teamId == 0) continue;

                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "HR", parse(cols.get(14)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "SB", parse(cols.get(17)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "AVG", parse(cols.get(26)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "OBP", parse(cols.get(27)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "SLG", parse(cols.get(28)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "OPS", parse(cols.get(29)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "wRC+", parse(cols.get(31)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "BetterWAR", parse(cols.get(32)), null);

                            } else if (type.equals("pitching") && cols.size() >= 36) {
                                String teamName = cols.get(1).text();
                                String season = "20" + cols.get(2).text();
                                int teamId = teamNameToId.getOrDefault(teamName, 0);
                                if (teamId == 0) continue;

                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "BB", parse(cols.get(23)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "SO", parse(cols.get(26)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "ERA", parse(cols.get(30)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "WHIP", parse(cols.get(34)), null);
                                teamStatsDAO.insertStat(teamId, Integer.parseInt(season), "PitcherWAR", parse(cols.get(35)), null);
                            }
                        }

                        Thread.sleep((int) (1000 + Math.random() * 2000));

                    } catch (Exception e) {
                        System.out.println("에러 발생 (year: " + year + ", type: " + type + ")");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("DB 연결 실패: " + e.getMessage());
        }
    }

    private static double parse(Element col) {
        String text = col.text().trim();
        if (text.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
