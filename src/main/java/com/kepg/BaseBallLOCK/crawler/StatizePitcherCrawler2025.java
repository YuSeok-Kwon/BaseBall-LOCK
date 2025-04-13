package com.kepg.BaseBallLOCK.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

import com.kepg.BaseBallLOCK.player.plyerDao.PlayerDAO;
import com.kepg.BaseBallLOCK.player.playerDto.PlayerDTO;
import com.kepg.BaseBallLOCK.player.stats.statsDao.StatsDAO;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StatizePitcherCrawler2025 {

    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/BaseBallLOCK?serverTimezone=Asia/Seoul";
    private static final String username = "root";
    private static final String password = "rnjs7944";

    private static final Map<String, Integer> colorToTeamIdMap = new HashMap<>();
    static {
        colorToTeamIdMap.put("#042071", 2); // 두산
        colorToTeamIdMap.put("#cf152d", 4); // SSG
        colorToTeamIdMap.put("#ed1c24", 1); // KIA
        colorToTeamIdMap.put("#002b69", 7); // NC
        colorToTeamIdMap.put("#000000", 8); // KT
        colorToTeamIdMap.put("#888888", 9); // 롯데
        colorToTeamIdMap.put("#f37321", 6); // 한화
        colorToTeamIdMap.put("#0061AA", 3); // 삼성
        colorToTeamIdMap.put("#fc1cad", 5); // LG
        colorToTeamIdMap.put("#86001f", 10); // 키움
    }

    @Scheduled(cron = "0 0 12 * * *") // 매일 12시 정각 실행
    public void runPitcherCrawler() {
        String[] urls = {
            "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=%d&lt=10100&reg=A",
            "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=ASC&year=%d&lt=10100&reg=A"
        };

        int[] years = {2025};

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            PlayerDAO playerDAO = new PlayerDAO(conn);
            StatsDAO statsDAO = new StatsDAO(conn);

            for (int year : years) {
                for (String baseUrl : urls) {
                    String url = String.format(baseUrl, year);
                    System.out.println("투수 크롤링 시작: " + year);

                    try {
                        Document doc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0")
                                .referrer("https://www.google.com")
                                .timeout(10000)
                                .get();

                        Element table = doc.selectFirst("table");
                        if (table == null) {
                            System.out.println("❌ 테이블 없음 (year: " + year + ")");
                            continue;
                        }

                        Elements rows = table.select("tr");
                        for (Element row : rows) {
                            Elements cols = row.select("td");
                            if (cols.size() > 36) {
                                String name = cols.get(1).text().trim();
                                String[] sp = cols.get(2).text().trim().split(" ");
                                String season = sp.length > 0 ? sp[0] : "";
                                String position = "P";

                                int teamId = 0;
                                Element colorElement = cols.get(1).selectFirst("span[style]");
                                if (colorElement != null) {
                                    String style = colorElement.attr("style");
                                    if (style.contains("background:")) {
                                        String bgColor = style.split("background:")[1].split(";")[0].trim();
                                        teamId = colorToTeamIdMap.getOrDefault(bgColor, 0);
                                    }
                                }

                                PlayerDTO player = new PlayerDTO();
                                player.setTeamId(teamId);
                                player.setName(name);
                                player.setPosition(position);
                                player.setSeason(Integer.parseInt(season));

                                int playerId = playerDAO.findOrCreatePlayerIdBySeason(player);

                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "G", Integer.parseInt(cols.get(4).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "W", Integer.parseInt(cols.get(10).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "L", Integer.parseInt(cols.get(11).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "SV", Integer.parseInt(cols.get(12).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "HLD", Integer.parseInt(cols.get(13).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "IP", parseDouble(cols.get(14).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "H", Integer.parseInt(cols.get(19).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "HR", Integer.parseInt(cols.get(22).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "BB", Integer.parseInt(cols.get(23).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "SO", Integer.parseInt(cols.get(26).text()), null);

                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "ERA", parseDouble(cols.get(30).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "WHIP", parseDouble(cols.get(35).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "WAR", parseDouble(cols.get(36).text()), null);
                            }
                        }

                        Thread.sleep((int) (1000 + Math.random() * 3000));

                    } catch (Exception e) {
                    	System.out.println("크롤링 중 에러 (year: " + year + ")");
                        e.printStackTrace();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("DB 연결 실패: " + e.getMessage());
        }
    }

    private double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}