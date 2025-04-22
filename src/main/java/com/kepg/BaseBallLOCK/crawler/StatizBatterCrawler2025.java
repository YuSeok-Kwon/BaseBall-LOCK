package com.kepg.BaseBallLOCK.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.dto.PlayerDTO;
import com.kepg.BaseBallLOCK.player.service.PlayerService;
import com.kepg.BaseBallLOCK.player.stats.service.BatterStatsService;
import com.kepg.BaseBallLOCK.player.stats.statsDto.BatterStatsDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizBatterCrawler2025 {

    private final PlayerService playerService;
    private final BatterStatsService statsService;

    private static final Map<String, Integer> colorToTeamIdMap = new HashMap<>();
    static {
        colorToTeamIdMap.put("#042071", 2); // 두산
        colorToTeamIdMap.put("#cf152d", 4); // SSG
        colorToTeamIdMap.put("#ff0000", 4); // SSG + SK
        colorToTeamIdMap.put("#ed1c24", 1); // KIA
        colorToTeamIdMap.put("#002b69", 7); // NC
        colorToTeamIdMap.put("#000000", 8); // KT
        colorToTeamIdMap.put("#888888", 9); // 로테
        colorToTeamIdMap.put("#f37321", 6); // 한화
        colorToTeamIdMap.put("#0061AA", 3); // 삼성
        colorToTeamIdMap.put("#fc1cad", 5); // LG
        colorToTeamIdMap.put("#86001f", 10); // 키움
    }
    
    @Scheduled(cron = "0 0 0 * * *")  // 매일 00시 정각
    public void runDailyCrawlingTask() {
        crawlStats();
    }

    public void crawlStats() {
        String baseUrl = "https://statiz.sporki.com/stats/?m=main&m2=batting&m3=default&so=WAR&ob=DESC&year=%d&po=%d&lt=10100&reg=A";
        int[] years = {2025};
        int[] positions = {2, 3, 4, 5, 6, 7, 8, 9, 10};

        for (int year : years) {
            for (int po : positions) {
                String url = String.format(baseUrl, year, po);
                System.out.println("\uD83D\uDCE1 \ud06c\ub864\ub9c1 \uc2dc\uc791: year=" + year + ", position=" + po);
                try {
                    Document doc = Jsoup.connect(url).timeout(10000).get();
                    Element table = doc.selectFirst("div.table_type01 > table");
                    if (table == null) continue;

                    for (Element row : table.select("tr")) {
                        Elements cols = row.select("td");
                        if (cols.size() <= 32) continue;

                        String name = cols.get(1).text().trim();
                        String[] sp = cols.get(2).text().trim().split(" ");
                        int season = Integer.parseInt(sp[0]);
                        String position = sp.length > 1 ? sp[1] : "?";

                        Element colorElem = cols.get(2).selectFirst("span[style]");
                        int teamId = 0;
                        if (colorElem != null) {
                            String bg = colorElem.attr("style").split("background:")[1].split(";")[0].trim();
                            teamId = colorToTeamIdMap.getOrDefault(bg, 0);
                        }

                        PlayerDTO playerDTO = PlayerDTO.builder()
                                .name(name)
                                .teamId(teamId)
                                .build();
                        Player player = playerService.findOrCreatePlayer(playerDTO);
                        int playerId = player.getId();

                        statsService.saveBatterStats(createStat(playerId, season, "position", 0, null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "G", parseInt(cols.get(4).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "PA", parseInt(cols.get(7).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "WAR", parseDouble(cols.get(3).text()), parseInt(cols.get(0).text()), position));
                        statsService.saveBatterStats(createStat(playerId, season, "H", parseInt(cols.get(11).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "2B", parseInt(cols.get(12).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "3B", parseInt(cols.get(13).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "HR", parseInt(cols.get(14).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "RBI", parseInt(cols.get(16).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "SB", parseInt(cols.get(17).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "BB", parseInt(cols.get(19).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "SO", parseInt(cols.get(22).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "AVG", parseDouble(cols.get(26).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "OBP", parseDouble(cols.get(27).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "SLG", parseDouble(cols.get(28).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "OPS", parseDouble(cols.get(29).text()), null, position));
                        statsService.saveBatterStats(createStat(playerId, season, "wRC+", parseDouble(cols.get(31).text()), null, position));
                    }

                } catch (Exception e) {
                    System.out.println("\uD83D\uDEA8 \uc5d0\ub7ec \ubc1c\uc0dd: year=" + year + ", po=" + po);
                    e.printStackTrace();
                }
            }
        }
    }
    
    private BatterStatsDTO createStat(int playerId, int season, String category, double value, Integer ranking, String position) {
        return BatterStatsDTO.builder()
                .playerId(playerId)
                .season(season)
                .category(category)
                .value(value)
                .ranking(ranking)
                .position(position)
                .build();
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return 0;
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