package com.kepg.BaseBallLOCK.crawler;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.game.highlight.dto.GameHighlightDTO;
import com.kepg.BaseBallLOCK.game.highlight.service.GameHighlightService;
import com.kepg.BaseBallLOCK.game.lineUp.service.LineupService;
import com.kepg.BaseBallLOCK.game.record.service.RecordService;
import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;
import com.kepg.BaseBallLOCK.game.schedule.service.ScheduleService;
import com.kepg.BaseBallLOCK.game.scoreBoard.domain.ScoreBoard;
import com.kepg.BaseBallLOCK.game.scoreBoard.service.ScoreBoardService;
import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.service.PlayerService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizGameCrawler {

    private final ScheduleService scheduleService;
    private final ScoreBoardService scoreBoardService;
    private final GameHighlightService gameHighlightService;
    private final PlayerService playerService;
    private final LineupService lineupService;
    private final RecordService recordService;

    private static final Map<String, Integer> teamNameToId = new HashMap<>();
    private static final Map<Integer, String> teamIdToStadium = new HashMap<>();

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

        teamIdToStadium.put(1, "광주 기아챔피언스필드");
        teamIdToStadium.put(2, "서울 잠실종합운동장");
        teamIdToStadium.put(3, "대구 삼성라이온즈파크");
        teamIdToStadium.put(4, "인천 SSG랜더스필드");
        teamIdToStadium.put(5, "서울 잠실종합운동장");
        teamIdToStadium.put(6, "대전 한화생명이글스파크");
        teamIdToStadium.put(7, "창원 NC파크");
        teamIdToStadium.put(8, "수원 KT위즈파크");
        teamIdToStadium.put(9, "부산 사직야구장");
        teamIdToStadium.put(10, "서울 고척스카이돔");
    }
    
    // statizId 추출할 날짜 범위 지정
    public void crawlGameRange(LocalDate startDate, LocalDate endDate) {
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            List<Integer> statizIds = getStatizIdsFromDailyPage(currentDate);

            System.out.println("[일자] " + currentDate + " → 추출된 경기 수: " + statizIds.size());
            for (int i = 0; i < statizIds.size(); i++) {
                int statizId = statizIds.get(i);
                System.out.println("▶ [" + (i + 1) + "/" + statizIds.size() + "] statizId = " + statizId);
                processGame(statizId);
            }

            currentDate = currentDate.plusDays(1);
        }
    }
    
    // 날짜별 statizId 추출
    public List<Integer> getStatizIdsFromDailyPage(LocalDate date) {
//    	List<Integer> statizIds = getStatizIdsFromDailyPage(LocalDate.now()); // 직접 실행용
        List<Integer> statizIds = new ArrayList<>();
        WebDriver driver = null;

        try {
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String url = "https://statiz.sporki.com/schedule/?m=daily&date=" + dateStr;

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
            driver.get(url);
            Thread.sleep(3000);

            String html = driver.getPageSource();
            Document doc = Jsoup.parse(html);

            Elements links = doc.select("div.box_type_boared a[href*=/schedule/?m=summary]");

            for (Element link : links) {
                String href = link.attr("href");
                if (href.contains("s_no=")) {
                    String[] parts = href.split("s_no=");
                    if (parts.length == 2) {
                        int statizId = Integer.parseInt(parts[1].trim());
                        statizIds.add(statizId);
                    }
                }
            }

            System.out.println("[statizId 추출 완료] " + statizIds);

        } catch (Exception e) {
            System.out.println("statizId 추출 중 오류 발생");
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
        }

        return statizIds;
    }
    
    public void processGame(int statizId) {
        WebDriver driver = null;

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);

            // ----------------------- summary 크롤링 ----------------------
            String summaryUrl = "https://statiz.sporki.com/schedule/?m=summary&s_no=" + statizId;
            driver.get(summaryUrl);
            Thread.sleep(3000);

            String html = driver.getPageSource();
            Document doc = Jsoup.parse(html);

            Element scoreTable = doc.selectFirst("div.box_type_boared > div.item_box.w100 .table_type03 table");
            if (scoreTable == null) return;

            Elements rows = scoreTable.select("tbody > tr");
            if (rows.size() < 2) return;

            Elements awayTds = rows.get(0).select("td");
            Elements homeTds = rows.get(1).select("td");

            String awayTeam = awayTds.get(0).text().trim();
            String homeTeam = homeTds.get(0).text().trim();

            int awayTeamId = teamNameToId.getOrDefault(awayTeam, 0);
            int homeTeamId = teamNameToId.getOrDefault(homeTeam, 0);
            if (awayTeamId == 0 || homeTeamId == 0) return;

            String datePart = doc.selectFirst(".callout_box .txt").text().split(",")[1].trim();
            int year = statizId / 10000;
            LocalDate matchDate = LocalDate.parse(year + "-" + datePart, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Timestamp matchDateTime = Timestamp.valueOf(matchDate.atStartOfDay());
            
            // ----------------------- schedule 크롤링 호출 ----------------------
            updateScheduleFromSummary(doc, homeTeamId, awayTeamId, matchDateTime, statizId);
            // ----------------------- ----------------- ----------------------

            
            Integer scheduleId = scheduleService.findIdByDateAndTeams(matchDateTime, homeTeamId, awayTeamId);
            if (scheduleId == null) return;

            List<Integer> awayScores = new ArrayList<>();
            List<Integer> homeScores = new ArrayList<>();
            for (int i = 1; i <= 9; i++) {
                Element awayScoreTd = awayTds.get(i).selectFirst(".score");
                Element homeScoreTd = homeTds.get(i).selectFirst(".score");

                String awayText = awayScoreTd == null ? "0" : awayScoreTd.ownText().trim();
                String homeText = homeScoreTd == null ? "0" : homeScoreTd.ownText().trim();

                awayScores.add(awayText.equals("-") ? 0 : Integer.parseInt(awayText));
                homeScores.add(homeText.equals("-") ? 0 : Integer.parseInt(homeText));
            }

            int awayR = Integer.parseInt(awayTds.get(13).selectFirst(".score").ownText().trim());
            int awayH = Integer.parseInt(awayTds.get(14).selectFirst(".score").ownText().trim());
            int awayE = Integer.parseInt(awayTds.get(15).selectFirst(".score").ownText().trim());
            int awayB = Integer.parseInt(awayTds.get(16).selectFirst(".score").ownText().trim());

            int homeR = Integer.parseInt(homeTds.get(13).selectFirst(".score").ownText().trim());
            int homeH = Integer.parseInt(homeTds.get(14).selectFirst(".score").ownText().trim());
            int homeE = Integer.parseInt(homeTds.get(15).selectFirst(".score").ownText().trim());
            int homeB = Integer.parseInt(homeTds.get(16).selectFirst(".score").ownText().trim());

            String winPitcher = getPitcherName(doc, ".game_result .win a");
            String losePitcher = getPitcherName(doc, ".game_result .lose a");

            ScoreBoard sb = ScoreBoard.builder()
                    .scheduleId(scheduleId)
                    .homeScore(homeR).awayScore(awayR)
                    .homeInningScores(toInningString(homeScores))
                    .awayInningScores(toInningString(awayScores))
                    .homeR(homeR).homeH(homeH).homeE(homeE).homeB(homeB)
                    .awayR(awayR).awayH(awayH).awayE(awayE).awayB(awayB)
                    .winPitcher(winPitcher)
                    .losePitcher(losePitcher)
                    .build();
            scoreBoardService.saveOrUpdate(sb);

            Element highlightBox = doc.selectFirst("div.sh_box:has(.box_head:contains(결정적 장면 Best 5)) table");
            if (highlightBox != null) {
                Elements highlightRows = highlightBox.select("tbody > tr");
                int ranking = 1;
                for (int i = 0; i < highlightRows.size(); i++) {
                    Elements tds = highlightRows.get(i).select("td");
                    GameHighlightDTO dto = GameHighlightDTO.builder()
                            .scheduleId(scheduleId)
                            .ranking(ranking++)
                            .inning(tds.get(0).text().trim())
                            .pitcherName(tds.get(1).text().trim())
                            .batterName(tds.get(2).text().trim())
                            .pitchCount(tds.get(3).text().trim())
                            .result(tds.get(4).text().trim())
                            .beforeSituation(tds.get(5).text().trim())
                            .afterSituation(tds.get(6).text().trim())
                            .build();
                    gameHighlightService.saveOrUpdate(dto);
                }
            }

            // ------------------------ boxscore 크롤링 ---------------------
            String boxscoreUrl = "https://statiz.sporki.com/schedule/?m=boxscore&s_no=" + statizId;
            driver.get(boxscoreUrl);
            Thread.sleep(3000);

            String boxHtml = driver.getPageSource();
            Document boxDoc = Jsoup.parse(boxHtml);

            Map<String, Integer> sbMap = extractStolenBases(boxDoc);
            saveBatterRecords(boxDoc, scheduleId, awayTeamId, sbMap);
            saveBatterRecords(boxDoc, scheduleId, homeTeamId, sbMap);
            savePitcherRecords(boxDoc, scheduleId);

        } catch (Exception e) {
            System.out.println("오류 발생: " + statizId);
            e.printStackTrace();
        } finally {
            if (driver != null) driver.quit();
        }
    }
    
    // 이닝 숫자 -> 문자열로 변경
    private String toInningString(List<Integer> scores) {
        String result = "";
        for (int i = 0; i < scores.size(); i++) {
            result += scores.get(i);
            if (i < scores.size() - 1) result += " ";
        }
        return result;
    }

    private String getPitcherName(Document doc, String selector) {        Element el = doc.selectFirst(selector);
        return el != null ? el.text() : null;
    }

    
    // 도루 정보 추출
    private Map<String, Integer> extractStolenBases(Document doc) {
        Map<String, Integer> sbMap = new HashMap<>();

        Elements logBoxes = doc.select("div.box_type_boared .log_box");
        for (Element box : logBoxes) {
            Elements divs = box.select("div.log_div");
            for (int i = 0; i < divs.size(); i++) {
                Element div = divs.get(i);
                Element strong = div.selectFirst("strong");
                if (strong != null && strong.text().contains("도루성공")) {
                    Elements spans = div.select("span");
                    for (int j = 0; j < spans.size(); j++) {
                        Element a = spans.get(j).selectFirst("a");
                        if (a != null) {
                            String name = a.text().trim();
                            if (sbMap.containsKey(name)) {
                                sbMap.put(name, sbMap.get(name) + 1);
                            } else {
                                sbMap.put(name, 1);
                            }
                        }
                    }
                }
            }
        }

        return sbMap;
    }

    // 타자 기록 저장
    private void saveBatterRecords(Document doc, int scheduleId, int teamId, Map<String, Integer> sbMap) {
        Elements sections = doc.select("div.box_type_boared");

        for (int i = 0; i < sections.size(); i++) {
            Element section = sections.get(i);
            Element head = section.selectFirst(".box_head");
            if (head == null || !head.text().contains("타격기록")) continue;

            String teamName = head.text().replaceAll(".*\\((.*?)\\).*", "$1").trim();
            Integer extractedTeamId = teamNameToId.get(teamName);
            if (extractedTeamId == null || !extractedTeamId.equals(teamId)) continue;

            Element table = section.selectFirst("table");
            if (table == null) continue;

            Elements rows = table.select("tbody > tr:not(.total)");
            for (int r = 0; r < rows.size(); r++) {
                Element row = rows.get(r);
                Elements cols = row.select("td");
                if (cols.size() < 22) continue;

                String name = cols.get(1).text().trim();

                Optional<Player> player = playerService.findByNameAndTeamId(name, teamId);
                if (player.isEmpty()) {
                    playerService.savePlayer(name, teamId);
                    player = playerService.findByNameAndTeamId(name, teamId);
                }

                try {
                    int pa = Integer.parseInt(cols.get(3).text().trim());
                    int ab = Integer.parseInt(cols.get(4).text().trim());
                    int hits = Integer.parseInt(cols.get(6).text().trim());
                    int hr = Integer.parseInt(cols.get(7).text().trim());
                    int rbi = Integer.parseInt(cols.get(8).text().trim());
                    int bb = Integer.parseInt(cols.get(9).text().trim());
                    int so = Integer.parseInt(cols.get(11).text().trim());
                    int sb = sbMap.containsKey(name) ? sbMap.get(name) : 0;
                    int order = cols.get(0).text().isEmpty() ? 0 : Integer.parseInt(cols.get(0).text().trim());
                    String pos = cols.get(2).text().trim();

                    lineupService.saveBatterLineup(scheduleId, teamId, order, pos, name);
                    recordService.saveBatterRecord(scheduleId, teamId, pa, ab, hits, hr, rbi, bb, so, sb, name);

                } catch (Exception e) {
                    System.out.println("타자 저장 중 에러: " + name + " - " + e.getMessage());
                }
            }
        }
    }

    // 투수 기록 저장
    private void savePitcherRecords(Document doc, int scheduleId) {
        Elements sections = doc.select("div.box_type_boared");

        for (int i = 0; i < sections.size(); i++) {
            Element section = sections.get(i);
            Element head = section.selectFirst(".box_head");
            if (head == null || !head.text().contains("투구기록")) continue;

            String teamName = head.text().replaceAll(".*\\((.*?)\\).*", "$1").trim();
            Integer teamId = teamNameToId.get(teamName);
            if (teamId == null) continue;

            Element table = section.selectFirst(".table_type03 table");
            if (table == null) continue;

            Elements rows = table.select("tbody > tr:not(.total)");
            for (int r = 0; r < rows.size(); r++) {
                Element row = rows.get(r);
                Elements cols = row.select("td");
                if (cols.size() < 18) continue;

                Element nameAnchor = cols.get(0).selectFirst("a");
                if (nameAnchor == null) continue;

                String fullText = cols.get(0).text();
                String name = fullText.replaceAll("\\s*\\([^)]*\\)", "").trim();
                String decision = "";
                if (fullText.contains("(승")) decision = "W";
                else if (fullText.contains("(패")) decision = "L";
                else if (fullText.contains("(세")) decision = "SV";
                else if (fullText.contains("(홀")) decision = "HLD";

                Optional<Player> player = playerService.findByNameAndTeamId(name, teamId);
                if (player.isEmpty()) {
                    playerService.savePlayer(name, teamId);
                    player = playerService.findByNameAndTeamId(name, teamId);
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

                } catch (Exception e) {
                    System.out.println("투수 저장 중 에러: " + name + " - " + e.getMessage());
                }
            }
        }
    }
    
    // schedule 정보 저장
    private void updateScheduleFromSummary(Document doc, int homeTeamId, int awayTeamId, Timestamp matchDateTime, int statizId) {
        String status = "종료";
        String stadium = null;

        try {
            Element callout = doc.selectFirst(".callout_box .txt");
            if (callout != null) {
                String calloutText = callout.text(); // e.g., "경기종료, 05-06, 18:30, 광주 기아챔피언스필드"
                String[] tokens = calloutText.split(",");

                if (tokens.length >= 3) {
                    status = tokens[0].trim(); // 예: 경기종료 or 경기전
                    String timeStr = tokens[2].trim(); // 예: "18:30"
                    String stadiumStr = tokens[3].trim(); // 예: "광주 기아챔피언스필드"
                    stadium = stadiumStr;

                    LocalTime gameTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                    matchDateTime = Timestamp.valueOf(matchDateTime.toLocalDateTime().toLocalDate().atTime(gameTime));
                }
            }
        } catch (Exception e) {
            System.out.println("경기 시간/구장 파싱 실패");
        }

        // 스코어 테이블 정보로 점수 파싱
        Element scoreTable = doc.selectFirst("div.box_type_boared > div.item_box.w100 .table_type03 table");
        Integer homeScore = null;
        Integer awayScore = null;

        if (scoreTable != null) {
            Elements rows = scoreTable.select("tbody > tr");
            if (rows.size() >= 2) {
                Elements awayTds = rows.get(0).select("td");
                Elements homeTds = rows.get(1).select("td");

                try {
                    awayScore = Integer.parseInt(awayTds.get(13).selectFirst(".score").ownText().trim());
                    homeScore = Integer.parseInt(homeTds.get(13).selectFirst(".score").ownText().trim());
                } catch (Exception e) {
                    System.out.println("점수 파싱 실패");
                }
            }
        }

        Schedule schedule = new Schedule();
        schedule.setMatchDate(matchDateTime);
        schedule.setHomeTeamId(homeTeamId);
        schedule.setAwayTeamId(awayTeamId);
        schedule.setHomeTeamScore(homeScore);
        schedule.setAwayTeamScore(awayScore);
        schedule.setStatizId(statizId);
        schedule.setStadium(stadium != null ? stadium : teamIdToStadium.getOrDefault(homeTeamId, "미정"));
        schedule.setStatus(status);

        scheduleService.saveOrUpdate(schedule);

        System.out.printf("[Schedule 업데이트 완료] %s %d vs %d (%s)\n",
                matchDateTime.toString(), homeTeamId, awayTeamId, status);
    }
}
