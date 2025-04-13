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
public class StatizBetterCrawler2025 {

    private static final String jdbcUrl = "jdbc:mysql://localhost:3306/BaseBallLOCK?serverTimezone=Asia/Seoul";
    private static final String username = "root";
    private static final String password = "rnjs7944";

    // 색상 기반 팀 ID 매핑 추가
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

    @Scheduled(cron = "0 0 12 * * *") // 매일 12시 정각에 실행
    public void runBattingCrawler() {
        String baseUrl = "https://statiz.sporki.com/stats/?m=main&m2=batting&m3=default&so=WAR&ob=DESC"
                + "&year=%d&po=%d&lt=10100&reg=A";

        int[] years = {2025};
        int[] positions = {2, 3, 4, 5, 6, 7, 8, 9, 10}; // 포수~지명타자

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            PlayerDAO playerDAO = new PlayerDAO(conn);
            StatsDAO statsDAO = new StatsDAO(conn);

            for (int year : years) {
                for (int po : positions) {
                    String url = String.format(baseUrl, year, po);
                    System.out.println("크롤링 시작: " + year + " / 포지션 코드: " + po);

                    try {
                        Document doc = Jsoup.connect(url)
                                .userAgent("Mozilla/5.0")
                                .referrer("https://www.google.com")
                                .timeout(10000)
                                .get();

                        Element table = doc.selectFirst("div.table_type01 > table");
                        if (table == null) {
                            System.out.println("테이블 없음 (year: " + year + ", po: " + po + ")");
                            continue;
                        }

                        Elements rows = table.select("tr");

                        for (Element row : rows) {
                            Elements cols = row.select("td");
                            if (cols.size() > 31) {
                                String name = cols.get(1).text().trim();
                                String[] sp = cols.get(2).text().trim().split(" ");
                                String season = sp.length > 0 ? sp[0] : "";
                                String position = sp.length > 1 ? sp[1] : "";

                                // 색상 기반 teamId 추출
                                Element teamColorElement = row.select("td").get(2).selectFirst("span[style]");
                                int teamId = 0;
                                if (teamColorElement != null) {
                                    String style = teamColorElement.attr("style");
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
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "PA", Integer.parseInt(cols.get(7).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "WAR", Double.parseDouble(cols.get(3).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "H", Integer.parseInt(cols.get(11).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "2B", Integer.parseInt(cols.get(12).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "3B", Integer.parseInt(cols.get(13).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "HR", Integer.parseInt(cols.get(14).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "RBI", Integer.parseInt(cols.get(16).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "SB", Integer.parseInt(cols.get(17).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "BB", Integer.parseInt(cols.get(19).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "SO", Integer.parseInt(cols.get(22).text()), null);

                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "AVG", parseDouble(cols.get(26).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "OBP", parseDouble(cols.get(27).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "SLG", parseDouble(cols.get(28).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "OPS", parseDouble(cols.get(29).text()), null);
                                statsDAO.insertOrUpdateStat(playerId, Integer.parseInt(season), "wRC+", parseDouble(cols.get(31).text()), null);
                            }
                        }

                        Thread.sleep((int) (1000 + Math.random() * 3000));
                    } catch (Exception e) {
                        System.out.println("크롤링 중 에러 (year: " + year + ", po: " + po + ")");
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