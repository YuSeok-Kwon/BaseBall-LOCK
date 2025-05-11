package com.kepg.BaseBallLOCK.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.dto.PlayerDTO;
import com.kepg.BaseBallLOCK.player.service.PlayerService;
import com.kepg.BaseBallLOCK.player.stats.service.BatterStatsService;
import com.kepg.BaseBallLOCK.player.stats.statsDto.BatterStatsDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizBatterCrawler {

    private final PlayerService playerService;
    private final BatterStatsService statsService;

    public void crawl() {
        String baseUrl = "https://statiz.sporki.com/stats/?m=main&m2=batting&m3=default&so=WAR&ob=DESC&year=%d&po=%d&lt=10100&reg=A&pr=50";
        int[] years = {2025};
        int[] positions = {2,3,4,5,6,7,8,9,10};

        for (int year : years) {
            for (int po : positions) {
                String url = String.format(baseUrl, year, po);
                System.out.println("크롤링 시작: year=" + year + ", position=" + po);
                WebDriver driver = null;
                try {
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--headless");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");

                    driver = new ChromeDriver(options);
                    driver.get(url);

                    Thread.sleep(5000);

                    Document doc = Jsoup.parse(driver.getPageSource());
                    Element table = doc.selectFirst("div.table_type01 > table");
                    if (table == null) {
                        System.out.println("테이블 못 찾음, url=" + url);
                        continue;
                    }

                    for (Element row : table.select("tr")) {
                        Elements cols = row.select("td");
                        if (cols.size() <= 32) continue;
                        processRow(cols);
                    }
                    System.out.println("batterStats 저장완료 : year=" + year + ", po=" + po);

                } catch (Exception e) {
                    System.out.println("에러 발생: year=" + year + ", po=" + po);
                    e.printStackTrace();
                } finally {
                    if (driver != null) driver.quit();
                }
            }
        }
    }
    
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
        colorToTeamIdMap.put("#86001f", 10); // 키울
    }

    private void processRow(Elements cols) {
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

        PlayerDTO dto = PlayerDTO.builder().name(name).teamId(teamId).build();
        Player player = playerService.findOrCreatePlayer(dto);
        int playerId = player.getId();

        save(playerId, season, "G", parseInt(cols.get(4).text()), null, position);
        save(playerId, season, "PA", parseInt(cols.get(7).text()), null, position);
        save(playerId, season, "WAR", parseDouble(cols.get(3).text()), parseInt(cols.get(0).text()), position);
        save(playerId, season, "H", parseInt(cols.get(11).text()), null, position);
        save(playerId, season, "2B", parseInt(cols.get(12).text()), null, position);
        save(playerId, season, "3B", parseInt(cols.get(13).text()), null, position);
        save(playerId, season, "HR", parseInt(cols.get(14).text()), null, position);
        save(playerId, season, "RBI", parseInt(cols.get(16).text()), null, position);
        save(playerId, season, "SB", parseInt(cols.get(17).text()), null, position);
        save(playerId, season, "BB", parseInt(cols.get(19).text()), null, position);
        save(playerId, season, "SO", parseInt(cols.get(22).text()), null, position);
        save(playerId, season, "AVG", parseDouble(cols.get(26).text()), null, position);
        save(playerId, season, "OBP", parseDouble(cols.get(27).text()), null, position);
        save(playerId, season, "SLG", parseDouble(cols.get(28).text()), null, position);
        save(playerId, season, "OPS", parseDouble(cols.get(29).text()), null, position);
        save(playerId, season, "wRC+", parseDouble(cols.get(31).text()), null, position);
    }

    private void save(int playerId, int season, String category, double value, Integer ranking, String position) {
        BatterStatsDTO dto = BatterStatsDTO.builder()
            .playerId(playerId)
            .season(season)
            .category(category)
            .value(value)
            .ranking(ranking)
            .position(position)
            .build();
        statsService.saveBatterStats(dto);
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
