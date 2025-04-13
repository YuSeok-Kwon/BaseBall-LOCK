package com.kepg.BaseBallLOCK.crawler;

import com.kepg.BaseBallLOCK.team.teamStatsDao.TeamStatsDAO;

import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Component
public class StatizTeamWaaCrawler2025 {

    @Autowired
    private DataSource dataSource;

    private Map<String, Integer> teamNameToId;

    @PostConstruct
    public void initTeamMap() {
        teamNameToId = new HashMap<>();
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
    }

    // 매일 오후 12시 실행
    @Scheduled(cron = "0 0 12 * * *")
    public void updateTeamWaa() {
        String url = "https://statiz.sporki.com/season/?m=teamoverall&year=2025";

        try (Connection conn = dataSource.getConnection()) {
            TeamStatsDAO teamStatsDAO = new TeamStatsDAO(conn);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .referrer("https://www.google.com")
                    .timeout(10000)
                    .get();

            Elements tables = doc.select("div.item_box table");
            if (tables.size() < 2) {
                System.out.println("팀 분석(WAA) 테이블이 없습니다.");
                return;
            }

            Element waaTable = tables.get(1); // 두 번째 테이블
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

                int season = 2025;

                teamStatsDAO.insertOrUpdateStat(teamId, season, "타격", hitting, null);
                teamStatsDAO.insertOrUpdateStat(teamId, season, "주루", running, null);
                teamStatsDAO.insertOrUpdateStat(teamId, season, "수비", defense, null);
                teamStatsDAO.insertOrUpdateStat(teamId, season, "선발", starting, null);
                teamStatsDAO.insertOrUpdateStat(teamId, season, "불펜", bullpen, null);

                System.out.printf("✅ 저장 완료 - 팀: %s\n", teamName);
            }

        } catch (Exception e) {
            System.out.println("❌ 에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

