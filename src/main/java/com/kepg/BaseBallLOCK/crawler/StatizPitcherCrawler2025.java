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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.dto.PlayerDTO;
import com.kepg.BaseBallLOCK.player.service.PlayerService;
import com.kepg.BaseBallLOCK.player.stats.service.PitcherStatsService;
import com.kepg.BaseBallLOCK.player.stats.statsDto.PitcherStatsDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizPitcherCrawler2025 {

    private final PlayerService playerService;
    private final PitcherStatsService statsService;

    private static final Map<Integer, String> teamUrls = new HashMap<>();
    static {
        teamUrls.put(5, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=5002&po=1&lt=10100&reg=A");   // LG
        teamUrls.put(6, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=7002&po=1&lt=10100&reg=A");   // 한화
        teamUrls.put(9, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=3001&po=1&lt=10100&reg=A");   // 롯데
        teamUrls.put(3, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=1001&po=1&lt=10100&reg=A");   // 삼성
        teamUrls.put(8, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=12001&po=1&lt=10100&reg=A");  // KT
        teamUrls.put(4, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=9002&po=1&lt=10100&reg=A");   // SSG
        teamUrls.put(1, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=2002&po=1&lt=10100&reg=A");   // KIA
        teamUrls.put(2, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=6002&po=1&lt=10100&reg=A");   // 두산
        teamUrls.put(7, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=11001&po=1&lt=10100&reg=A");  // NC
        teamUrls.put(10, "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=2025&te=10001&po=1&lt=10100&reg=A");  // 키움 (두산과 te 중복 사용)
    }
    
    @Scheduled(cron = "0 20 23 * * *")  
    public void runDailyCrawlingTask() {
        crawlStats();
    }

    public void crawlStats() {
        int[] years = {2025};

        for (Map.Entry<Integer, String> entry : teamUrls.entrySet()) {
        	int teamId = entry.getKey();
            String url = entry.getValue();

            for (int year : years) {
            	System.out.println("크롤링 시작 - teamId=" + teamId + ", year=" + year);
            	
                WebDriver driver = null;
                try {
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--headless");
                    options.addArguments("--no-sandbox");
                    options.addArguments("--disable-dev-shm-usage");

                    driver = new ChromeDriver(options);
                    driver.get(url);

                    Thread.sleep(10000);
                    
                    String pageSource = driver.getPageSource();
                	Document doc = Jsoup.parse(pageSource);
                    
                	Element table = doc.selectFirst("table");
                    if (table == null) {
                        continue;
                    }
                    
                    Elements rows = table.select("tr");
                    for (Element row : rows) {
                        Elements cols = row.select("td");
                        if (cols.size() <= 36) continue;

                        processRow(cols, 2025, teamId); // 여기가 반드시 호출돼야 함
                    }

                } catch (Exception e) {
                    System.out.println("에러 발생: teamId=" + teamId + ", year=" + year);
                    e.printStackTrace();
                } finally {
                    if (driver != null) driver.quit();
                }
            }
        }
    }

    private void processRow(Elements cols, int year, int teamId) {
        String name = cols.get(1).text().trim();
        System.out.println("processRow 진입 - 선수명: " + cols.get(1).text());
        int season = 2025;
        String position = "P";

        PlayerDTO dto = PlayerDTO.builder().name(name).teamId(teamId).build();
        Player player = playerService.findOrCreatePlayer(dto);
        int playerId = player.getId();

        save(playerId, season, "G", parseInt(cols.get(4).text()), null, position);
        save(playerId, season, "GS", parseInt(cols.get(5).text()), null, position);
        save(playerId, season, "GR", parseInt(cols.get(6).text()), null, position);
        save(playerId, season, "W", parseInt(cols.get(10).text()), null, position);
        save(playerId, season, "L", parseInt(cols.get(11).text()), null, position);
        save(playerId, season, "SV", parseInt(cols.get(12).text()), null, position);
        save(playerId, season, "HLD", parseInt(cols.get(13).text()), null, position);
        save(playerId, season, "IP", parseDouble(cols.get(14).text()), null, position);
        save(playerId, season, "H", parseInt(cols.get(19).text()), null, position);
        save(playerId, season, "HR", parseInt(cols.get(22).text()), null, position);
        save(playerId, season, "BB", parseInt(cols.get(23).text()), null, position);
        save(playerId, season, "SO", parseInt(cols.get(26).text()), null, position);
        save(playerId, season, "ERA", parseDouble(cols.get(30).text()), null, position);
        save(playerId, season, "WHIP", parseDouble(cols.get(35).text()), null, position);
        save(playerId, season, "WAR", parseDouble(cols.get(36).text()), null, position);
    }

    private void save(int playerId, int season, String category, double value, Integer ranking, String position) {
        PitcherStatsDTO dto = PitcherStatsDTO.builder()
                .playerId(playerId)
                .season(season)
                .category(category)
                .value(value)
                .ranking(ranking)
                .position(position)
                .build();
        statsService.savePitcherStats(dto);
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
