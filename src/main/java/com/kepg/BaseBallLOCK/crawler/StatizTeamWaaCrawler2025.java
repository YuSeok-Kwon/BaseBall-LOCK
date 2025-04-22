package com.kepg.BaseBallLOCK.crawler;

import com.kepg.BaseBallLOCK.team.teamStats.domain.TeamStats;
import com.kepg.BaseBallLOCK.team.teamStats.repository.TeamStatsRepository;
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
public class StatizTeamWaaCrawler2025 {

    private final TeamStatsRepository teamStatsRepository;

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

    @Scheduled(cron = "0 5 0 * * *")  
    public void run() {
        int[] years = {2025};

        for (int year : years) {
            try {
                String url = String.format("https://statiz.sporki.com/season/?m=teamoverall&year=%d", year);
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .referrer("https://www.google.com")
                        .timeout(10000)
                        .get();

                Elements tables = doc.select("div.item_box table");
                if (tables.size() < 4) {
                    System.out.println("팀 분석(WAA) 테이블 없음: " + year);
                    continue;
                }

                Element waaTable = tables.get(3);
                Elements rows = waaTable.select("tbody > tr");

                for (Element row : rows) {
                    Elements cols = row.select("td");
                    if (cols.size() < 6) continue;

                    String teamName = cols.get(0).text().trim();
                    Integer teamId = teamNameToId.get(teamName);
                    if (teamId == null) continue;

                    saveStat(teamId, year, "타격", parse(cols.get(1)));
                    saveStat(teamId, year, "주루", parse(cols.get(2)));
                    saveStat(teamId, year, "수비", parse(cols.get(3)));
                    saveStat(teamId, year, "선발", parse(cols.get(4)));
                    saveStat(teamId, year, "불펜", parse(cols.get(5)));

                    System.out.printf("저장 완료: %s (%d)\n", teamName, year);
                }

            } catch (Exception e) {
                System.out.println("에러 발생: year=" + year + " → " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void saveStat(Integer teamId, Integer season, String category, double value) {
        TeamStats entity = teamStatsRepository
                .findByTeamIdAndSeasonAndCategory(teamId, season, category)
                .map(existing -> {
                    existing.setValue(value);
                    return existing;
                })
                .orElse(
                        TeamStats.builder()
                                .teamId(teamId)
                                .season(season)
                                .category(category)
                                .value(value)
                                .build()
                );

        teamStatsRepository.save(entity);
    }

    private double parse(Element element) {
        try {
            return Double.parseDouble(element.text().trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}