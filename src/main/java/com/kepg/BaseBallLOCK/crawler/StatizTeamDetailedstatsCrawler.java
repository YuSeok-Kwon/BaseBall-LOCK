package com.kepg.BaseBallLOCK.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.kepg.BaseBallLOCK.team.teamStats.service.TeamStatsService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StatizTeamDetailedstatsCrawler  {

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

    public void run(String... args) throws Exception {
        String baseUrl = "https://statiz.sporki.com/stats/?m=team&m2=%s&m3=default&so=WAR&ob=DESC&year=%d";
        String[] types = {"batting", "pitching"};
        int[] years = {2020, 2021, 2022, 2023, 2024};

        for (String type : types) {
            for (int year : years) {
                String url = String.format(baseUrl, type, year);
                System.out.println("크롤링: " + year + ", " + type);
                Document doc = Jsoup.connect(url)
        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123 Safari/537.36")
        .referrer("https://www.google.com")
        .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
        .header("Connection", "keep-alive")
        .timeout(70000)
        .get();
                Elements tables = doc.select("div.table_type01 > table");
                if (tables.size() < 2) {
                	continue;
                }
                Element table = tables.get(1);

                for (Element row : table.select("tbody > tr")) {
                    Elements cols = row.select("td");
                    if (type.equals("batting") && cols.size() >= 33) {
                        handleBattingRow(cols, year);
                    } else if (type.equals("pitching") && cols.size() >= 36) {
                        handlePitchingRow(cols, year);
                    }
                }
            }
        }
    }

    private void handleBattingRow(Elements cols, int year) {
        String teamName = cols.get(1).text().trim();
        int teamId = teamNameToId.getOrDefault(teamName, 0);
        if (teamId == 0) {
        	return;
        }

        teamStatsService.saveOrUpdate(teamId, year, "HR", parse(cols.get(14)), null);
        teamStatsService.saveOrUpdate(teamId, year, "SB", parse(cols.get(17)), null);
        teamStatsService.saveOrUpdate(teamId, year, "AVG", parse(cols.get(26)), null);
        teamStatsService.saveOrUpdate(teamId, year, "OBP", parse(cols.get(27)), null);
        teamStatsService.saveOrUpdate(teamId, year, "SLG", parse(cols.get(28)), null);
        teamStatsService.saveOrUpdate(teamId, year, "OPS", parse(cols.get(29)), null);
        teamStatsService.saveOrUpdate(teamId, year, "wRC+", parse(cols.get(31)), null);
        teamStatsService.saveOrUpdate(teamId, year, "BetterWAR", parse(cols.get(32)), null);
    }

    private void handlePitchingRow(Elements cols, int year) {
        String teamName = cols.get(1).text().trim();
        int teamId = teamNameToId.getOrDefault(teamName, 0);
        if (teamId == 0) {
        	return;
        }

        teamStatsService.saveOrUpdate(teamId, year, "BB", parse(cols.get(23)), null);
        teamStatsService.saveOrUpdate(teamId, year, "SO", parse(cols.get(26)), null);
        teamStatsService.saveOrUpdate(teamId, year, "ERA", parse(cols.get(30)), null);
        teamStatsService.saveOrUpdate(teamId, year, "WHIP", parse(cols.get(34)), null);
        teamStatsService.saveOrUpdate(teamId, year, "PitcherWAR", parse(cols.get(35)), null);
    }

    private double parse(Element col) {
        try {
            return Double.parseDouble(col.text().trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}