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
public class StatizTeamWaaCrawler  {

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
        int[] years = {2020, 2021, 2022, 2023, 2024};
        String baseUrl = "https://statiz.sporki.com/season/?m=teamoverall&year=%d";

        for (int year : years) {
            String url = String.format(baseUrl, year);
            System.out.println("시즌 " + year + " 팀 분석(WAA) 시작");

            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .referrer("https://www.google.com")
                        .timeout(10000)
                        .get();

                Elements tables = doc.select("div.item_box table");
                if (tables.size() < 4) {
                    System.out.println("분석 테이블 없음 (year: " + year + ")");
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

                    double hitting  = parse(cols.get(1));
                    double running  = parse(cols.get(2));
                    double defense  = parse(cols.get(3));
                    double starting = parse(cols.get(4));
                    double bullpen  = parse(cols.get(5));

                    teamStatsService.saveOrUpdate(teamId, year, "타격", hitting, null);
                    teamStatsService.saveOrUpdate(teamId, year, "주루", running, null);
                    teamStatsService.saveOrUpdate(teamId, year, "수비", defense, null);
                    teamStatsService.saveOrUpdate(teamId, year, "선발", starting, null);
                    teamStatsService.saveOrUpdate(teamId, year, "불펜", bullpen, null);

                    System.out.printf("저장 완료 - [%s] (%d)\n", teamName, year);
                }

            } catch (Exception e) {
                System.out.println("크롤링 실패 (year: " + year + "): " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private double parse(Element col) {
        String text = col.text().trim();
        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            return 0.0;
        }
    }
}