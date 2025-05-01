package com.kepg.BaseBallLOCK.crawler;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.game.lineUp.service.LineupService;
import com.kepg.BaseBallLOCK.game.record.service.RecordService;
import com.kepg.BaseBallLOCK.game.schedule.service.ScheduleService;
import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.service.PlayerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizPlayerGameRecordCrawlerToday {

    private final ScheduleService scheduleService;
    private final PlayerService playerService;
    private final LineupService lineupService;
    private final RecordService recordService;

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

    @Scheduled(cron = "0 10 0 * * *")
    public void crawlYesterDay() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        int year = yesterday.getYear();
        int month = yesterday.getMonthValue();

        String calendarUrl = String.format("https://statiz.sporki.com/schedule/?year=%d&month=%d", year, month);

        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
            driver.get(calendarUrl);
            Thread.sleep(5000);

            Document doc = Jsoup.parse(driver.getPageSource());
            Elements gameLinks = doc.select(".calendar_area td:has(span.day) .games a[href*='s_no']");

            for (Element link : gameLinks) {
                String href = link.attr("href");
                String sNo = href.replaceAll(".*s_no=(\\d+)", "$1");
                String boxUrl = "https://statiz.sporki.com/schedule/?m=boxscore&s_no=" + sNo;

                driver.get(boxUrl);
                Thread.sleep(3000);

                Document gameDoc = Jsoup.parse(driver.getPageSource());
                Timestamp matchDate = extractMatchDate(gameDoc);
                if (matchDate == null || !matchDate.toLocalDateTime().toLocalDate().equals(yesterday)) {
                	continue;
                }

                Integer[] teamIds = extractTeamIds(gameDoc);
                Integer awayTeamId = teamIds[0];
                Integer homeTeamId = teamIds[1];
                if (awayTeamId == null || homeTeamId == null) continue;

                Integer scheduleId = scheduleService.findScheduleIdByMatchInfo(matchDate, homeTeamId, awayTeamId);
                if (scheduleId == null) continue;

                Map<String, Integer> sbMap = extractStolenBases(gameDoc);

                saveBatterRecords(gameDoc, scheduleId, awayTeamId, sbMap);
                saveBatterRecords(gameDoc, scheduleId, homeTeamId, sbMap);
                savePitcherRecords(gameDoc, scheduleId);

                System.out.println("저장 완료: " + sNo + " → ScheduleId: " + scheduleId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
        }
    }

    private Timestamp extractMatchDate(Document doc) {
        try {
            Element calloutBox = doc.selectFirst(".callout_box .txt");
            if (calloutBox == null) return null;
            String[] parts = calloutBox.text().split(",");
            String mmdd = parts[1].trim();
            LocalDate date = LocalDate.parse("2025-" + mmdd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return Timestamp.valueOf(date.atStartOfDay());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer[] extractTeamIds(Document doc) {
        Elements headers = doc.select("div.box_type_boared .box_head");
        if (headers.size() < 2) return new Integer[]{null, null};
        String away = headers.get(0).text().replace("타격기록", "").replaceAll("[()]", "").trim();
        String home = headers.get(1).text().replace("타격기록", "").replaceAll("[()]", "").trim();
        return new Integer[]{teamNameToId.get(away), teamNameToId.get(home)};
    }

    private Map<String, Integer> extractStolenBases(Document doc) {
        Map<String, Integer> sbMap = new HashMap<>();
        Elements logBoxes = doc.select("div.box_type_boared .log_box");
        for (Element box : logBoxes) {
            for (Element div : box.select("div.log_div")) {
                if (div.selectFirst("strong") != null && div.selectFirst("strong").text().contains("도루성공")) {
                    for (Element span : div.select("span")) {
                        String name = span.selectFirst("a").text().trim();
                        sbMap.put(name, sbMap.getOrDefault(name, 0) + 1);
                    }
                }
            }
        }
        return sbMap;
    }

    private void saveBatterRecords(Document doc, int scheduleId, int teamId, Map<String, Integer> sbMap) {
        Elements sections = doc.select("div.box_type_boared");
        for (Element section : sections) {
            Element head = section.selectFirst(".box_head");
            if (head == null || !head.text().contains("타격기록")) continue;

            String teamName = head.text().replaceAll(".*\\((.*?)\\).*", "$1").trim();
            Integer extractedTeamId = teamNameToId.get(teamName);
            if (!Objects.equals(extractedTeamId, teamId)) continue;

            Element table = section.selectFirst("table");
            if (table == null) continue;

            for (Element row : table.select("tbody > tr:not(.total)")) {
                Elements cols = row.select("td");
                if (cols.size() < 22) continue;

                while (true) {
                    String name = cols.get(1).text().trim();
                    Optional<Player> player = playerService.findByNameAndTeamId(name, teamId);
                    if (player.isEmpty()) {
                        playerService.savePlayer(name, teamId);
                        continue;
                    }
                    try {
                        int pa = Integer.parseInt(cols.get(3).text().trim());
                        int ab = Integer.parseInt(cols.get(4).text().trim());
                        int hits = Integer.parseInt(cols.get(6).text().trim());
                        int hr = Integer.parseInt(cols.get(7).text().trim());
                        int rbi = Integer.parseInt(cols.get(8).text().trim());
                        int bb = Integer.parseInt(cols.get(9).text().trim());
                        int so = Integer.parseInt(cols.get(11).text().trim());
                        int sb = sbMap.getOrDefault(name, 0);
                        int order = cols.get(0).text().isEmpty() ? 0 : Integer.parseInt(cols.get(0).text().trim());
                        String pos = cols.get(2).text().trim();

                        lineupService.saveBatterLineup(scheduleId, teamId, order, pos, name);
                        recordService.saveBatterRecord(scheduleId, teamId, pa, ab, hits, hr, rbi, bb, so, sb, name);
                        break;
                    } catch (Exception e) {
                        System.out.println("[타자 저장 중 에러] " + name);
                        break;
                    }
                }
            }
        }
    }

    private void savePitcherRecords(Document doc, int scheduleId) {
        for (Element section : doc.select("div.box_type_boared")) {
            Element head = section.selectFirst(".box_head");
            if (head == null || !head.text().contains("투구기록")) continue;

            String teamName = head.text().replaceAll(".*\\((.*?)\\).*", "$1").trim();
            Integer teamId = teamNameToId.get(teamName);
            if (teamId == null) continue;

            Element table = section.selectFirst(".table_type03 table");
            if (table == null) continue;

            for (Element row : table.select("tbody > tr:not(.total)")) {
                Elements cols = row.select("td");
                if (cols.size() < 18) continue;

                while (true) {
                    Element nameAnchor = cols.get(0).selectFirst("a");
                    if (nameAnchor == null) break;
                    String fullText = cols.get(0).text();
                    String decision = fullText.contains("(승") ? "W" : fullText.contains("(패") ? "L" : fullText.contains("(세") ? "SV" : fullText.contains("(홀") ? "HLD" : "";
                    String name = fullText.replaceAll("\\s*\\([^)]*\\)", "").trim();

                    Optional<Player> player = playerService.findByNameAndTeamId(name, teamId);
                    if (player.isEmpty()) {
                        playerService.savePlayer(name, teamId);
                        continue;
                    }
                    try {
                        double innings = Double.parseDouble(cols.get(1).text().trim());
                        int strikeouts = Integer.parseInt(cols.get(8).text().trim());
                        int bb = Integer.parseInt(cols.get(6).text().trim());
                        int hbp = Integer.parseInt(cols.get(7).text().trim());
                        int runs = Integer.parseInt(cols.get(4).text().trim());
                        int er = Integer.parseInt(cols.get(5).text().trim());
                        int hits = Integer.parseInt(cols.get(3).text().trim());
                        int hr = Integer.parseInt(cols.get(9).text().trim());

                        recordService.savePitcherRecord(scheduleId, teamId, name, innings, strikeouts, bb, hbp, runs, er, hits, hr, decision);
                        break;
                    } catch (Exception e) {
                        System.out.println("[투수 저장 중 에러] " + name);
                        break;
                    }
                }
            }
        }
    }
}