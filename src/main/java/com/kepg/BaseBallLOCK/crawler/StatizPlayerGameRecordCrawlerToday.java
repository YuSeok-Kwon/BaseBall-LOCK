package com.kepg.BaseBallLOCK.crawler;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    @Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")
    public void crawlYesterdayGames() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String dateStr = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Timestamp matchDateTime = Timestamp.valueOf(yesterday.atStartOfDay());

        for (int statizId = 20250001; statizId <= 20259999; statizId++) {
            try {
                String url = String.format("https://statiz.sporki.com/schedule/?m=boxscore&s_no=%d", statizId);
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .referrer("https://www.google.com")
                        .timeout(70000)
                        .get();

                Element calloutBox = doc.selectFirst(".callout_box .txt");
                if (calloutBox == null || !calloutBox.text().contains(dateStr.substring(5))) continue;

                String[] parts = calloutBox.text().split(",");
                LocalDate matchDate = LocalDate.parse("2025-" + parts[1].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if (!matchDate.equals(yesterday)) continue;

                Elements headers = doc.select("div.box_type_boared .box_head");
                if (headers.size() < 2) continue;

                String awayTeamName = headers.get(0).text().replace("타격기록", "").replaceAll("[()]", "").trim();
                String homeTeamName = headers.get(1).text().replace("타격기록", "").replaceAll("[()]", "").trim();

                Integer awayTeamId = teamNameToId.get(awayTeamName);
                Integer homeTeamId = teamNameToId.get(homeTeamName);
                if (awayTeamId == null || homeTeamId == null) continue;

                Integer scheduleId = scheduleService.findScheduleIdByMatchInfo(matchDateTime, homeTeamId, awayTeamId);
                if (scheduleId == null) continue;

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

                Elements tables = doc.select("div.box_type_boared .table_type03 table");
                for (int teamIndex = 0; teamIndex < 2; teamIndex++) {
                    int teamId = (teamIndex == 0) ? awayTeamId : homeTeamId;
                    Element table = tables.get(teamIndex);
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
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                }

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
                        	if (nameAnchor == null) continue;

                        	String name = nameAnchor.text().trim();
                        	String fullText = cols.get(0).text();
                        	String decision = "";

                        	if (fullText.contains("(승")) decision = "W";
                        	else if (fullText.contains("(패")) decision = "L";
                        	else if (fullText.contains("(세")) decision = "SV";
                        	else if (fullText.contains("(홀")) decision = "H";

                        	name = fullText.replaceAll("\\s*\\([^)]*\\)", "").trim();

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
                                e.printStackTrace();
                                break;
                            }
                        }
                    }
                }

                Thread.sleep(1500);
            } catch (Exception e) {
                System.out.println("오류 발생: " + statizId);
                e.printStackTrace();
            }
        }
    }
}