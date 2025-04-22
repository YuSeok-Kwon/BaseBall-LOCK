package com.kepg.BaseBallLOCK.crawler;

import com.kepg.BaseBallLOCK.team.teamStats.service.TeamStatsService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StatizTeamDetailedstatsCrawler2025 {

    private final TeamStatsService teamStatsService;

    private static final Map<String, Integer> teamNameToId = new HashMap<>();
    static {
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

    private final String[] types = { "batting", "pitching" };
    private final int[] years = {2025};

    @Scheduled(cron = "0 5 0 * * *") 
    public void runScheduledTeamStatCrawler() {
        for (String type : types) {
            for (int year : years) {
                String url = String.format("https://statiz.sporki.com/stats/?m=team&m2=%s&m3=default&so=WAR&ob=DESC&year=%d", type, year);
                try {
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0")
                            .referrer("https://www.google.com")
                            .timeout(10000)
                            .get();

                    Elements tables = doc.select("div.table_type01 > table");
                    if (tables.size() < 2) continue;

                    Element table = tables.get(1);
                    for (Element row : table.select("tbody > tr")) {
                        Elements cols = row.select("td");
                        if (cols.isEmpty()) continue;

                        String teamName = cols.get(1).text();
                        int teamId = teamNameToId.getOrDefault(teamName, 0);
                        if (teamId == 0) continue;

                        int season = Integer.parseInt("20" + cols.get(2).text());

                        if (type.equals("batting") && cols.size() >= 33) {
                            teamStatsService.saveOrUpdate(teamId, season, "HR", parseDouble(cols.get(14)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "SB", parseDouble(cols.get(17)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "AVG", parseDouble(cols.get(26)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "OBP", parseDouble(cols.get(27)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "SLG", parseDouble(cols.get(28)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "OPS", parseDouble(cols.get(29)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "wRC+", parseDouble(cols.get(31)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "BetterWAR", parseDouble(cols.get(32)), null);
                        } else if (type.equals("pitching") && cols.size() >= 36) {
                            teamStatsService.saveOrUpdate(teamId, season, "BB", parseDouble(cols.get(23)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "SO", parseDouble(cols.get(26)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "ERA", parseDouble(cols.get(30)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "WHIP", parseDouble(cols.get(34)), null);
                            teamStatsService.saveOrUpdate(teamId, season, "PitcherWAR", parseDouble(cols.get(35)), null);
                        }
                    }

                } catch (Exception e) {
                    System.out.println("오류 발생: year=" + year + ", type=" + type);
                    e.printStackTrace();
                }
            }
        }
    }

    private double parseDouble(Element col) {
        try {
            return Double.parseDouble(col.text().trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}