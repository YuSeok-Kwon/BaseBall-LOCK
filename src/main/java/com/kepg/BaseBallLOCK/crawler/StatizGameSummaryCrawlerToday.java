//package com.kepg.BaseBallLOCK.crawler;
//
//import java.sql.Timestamp;
//import java.time.LocalDate;
//import java.util.*;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.kepg.BaseBallLOCK.game.schedule.service.ScheduleService;
//import com.kepg.BaseBallLOCK.game.scoreBoard.domain.ScoreBoard;
//import com.kepg.BaseBallLOCK.game.scoreBoard.service.ScoreBoardService;
//import com.kepg.BaseBallLOCK.game.highlight.dto.GameHighlightDTO;
//import com.kepg.BaseBallLOCK.game.highlight.service.GameHighlightService;
//
//import lombok.RequiredArgsConstructor;
//
//@Component
//@RequiredArgsConstructor
//public class StatizGameSummaryCrawlerToday {
//
//    private final ScheduleService scheduleService;
//    private final ScoreBoardService scoreBoardService;
//    private final GameHighlightService gameHighlightService;
//
//    private static final Map<String, Integer> teamNameToId = new HashMap<>();
//    static {
//        teamNameToId.put("KIA", 1);
//        teamNameToId.put("두산", 2);
//        teamNameToId.put("삼성", 3);
//        teamNameToId.put("SSG", 4);
//        teamNameToId.put("LG", 5);
//        teamNameToId.put("한화", 6);
//        teamNameToId.put("NC", 7);
//        teamNameToId.put("KT", 8);
//        teamNameToId.put("롯데", 9);
//        teamNameToId.put("키울", 10);
//    }
//
//    @Scheduled(cron = "0 8 0 * * *")
//    public void crawl() {
//        WebDriver driver = null;
//        try {
//            ChromeOptions options = new ChromeOptions();
//            options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
//            driver = new ChromeDriver(options);
//
//            LocalDate yesterday = LocalDate.now().minusDays(1);
//            int year = yesterday.getYear();
//            int month = yesterday.getMonthValue();
//            int day = yesterday.getDayOfMonth();
//
//            String calendarUrl = String.format("https://statiz.sporki.com/schedule/?year=%d&month=%d", year, month);
//            driver.get(calendarUrl);
//            Thread.sleep(3000);
//
//            Document doc = Jsoup.parse(driver.getPageSource());
//
//            Elements tds = doc.select("table tbody td");
//            List<Integer> summaryIds = new ArrayList<>();
//
//            for (Element td : tds) {
//                Element daySpan = td.selectFirst("span.day");
//                if (daySpan != null && Integer.parseInt(daySpan.text().trim()) == day) {
//                    Elements links = td.select("a[href*='m=summary']");
//                    for (Element link : links) {
//                        String href = link.attr("href");
//                        String sNo = href.replaceAll(".*s_no=([0-9]+)", "$1");
//                        summaryIds.add(Integer.parseInt(sNo));
//                    }
//                    break;
//                }
//            }
//
//            for (int statizId : summaryIds) {
//                processSummaryPage(driver, statizId, yesterday);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("스코어보드 저장 실패");
//
//        } finally {
//            if (driver != null) driver.quit();
//        }
//    }
//
//    private void processSummaryPage(WebDriver driver, int statizId, LocalDate matchDate) {
//        try {
//            String gameUrl = "https://statiz.sporki.com/schedule/?m=summary&s_no=" + statizId;
//            driver.get(gameUrl);
//            Thread.sleep(3000);
//
//            Document gameDoc = Jsoup.parse(driver.getPageSource());
//
//            Element scoreTable = gameDoc.selectFirst("div.box_type_boared > div.item_box.w100 .table_type03 table");
//            if (scoreTable == null) return;
//
//            Elements rows = scoreTable.select("tbody > tr");
//            if (rows.size() < 2) return;
//
//            Element awayRow = rows.get(0);
//            Element homeRow = rows.get(1);
//            Elements awayTds = awayRow.select("td");
//            Elements homeTds = homeRow.select("td");
//
//            String awayTeam = awayTds.get(0).text().trim();
//            String homeTeam = homeTds.get(0).text().trim();
//            int homeTeamId = teamNameToId.getOrDefault(homeTeam, 0);
//            int awayTeamId = teamNameToId.getOrDefault(awayTeam, 0);
//            if (homeTeamId == 0 || awayTeamId == 0) return;
//
//            List<Integer> awayScores = new ArrayList<>();
//            List<Integer> homeScores = new ArrayList<>();
//            parseScores(awayTds, awayScores);
//            parseScores(homeTds, homeScores);
//
//            int awayR = getInt(awayTds.get(13));
//            int awayH = getInt(awayTds.get(14));
//            int awayE = getInt(awayTds.get(15));
//            int awayB = getInt(awayTds.get(16));
//
//            int homeR = getInt(homeTds.get(13));
//            int homeH = getInt(homeTds.get(14));
//            int homeE = getInt(homeTds.get(15));
//            int homeB = getInt(homeTds.get(16));
//
//            String winPitcher = extractPitcher(gameDoc, ".game_result .win a");
//            String losePitcher = extractPitcher(gameDoc, ".game_result .lose a");
//
//            Timestamp matchDateTime = Timestamp.valueOf(matchDate.atStartOfDay());
//            Integer scheduleId = scheduleService.findIdByDateAndTeams(matchDateTime, homeTeamId, awayTeamId);
//            if (scheduleId == null) {
//            	return;
//            }
//
//            saveScoreBoard(scheduleId, homeScores, awayScores,
//                    homeR, homeH, homeE, homeB,
//                    awayR, awayH, awayE, awayB,
//                    winPitcher, losePitcher);
//            System.out.println("스코어 + 결정적장면 저장 완료: " + statizId);
//
//            saveGameHighlights(gameDoc, scheduleId);
//            System.out.println("결정적장면 저장 완료: " + statizId);
//
//            Thread.sleep(2000);
//
//        } catch (Exception e) {
//            System.out.println("오류 발생: " + statizId);
//            e.printStackTrace();
//        }
//    }
//
//    private void saveScoreBoard(Integer scheduleId, List<Integer> homeScores, List<Integer> awayScores,
//                                int homeR, int homeH, int homeE, int homeB,
//                                int awayR, int awayH, int awayE, int awayB,
//                                String winPitcher, String losePitcher) {
//
//        ScoreBoard scoreBoard = ScoreBoard.builder()
//                .scheduleId(scheduleId)
//                .homeScore(homeR).awayScore(awayR)
//                .homeInningScores(toInningString(homeScores))
//                .awayInningScores(toInningString(awayScores))
//                .homeR(homeR).homeH(homeH).homeE(homeE).homeB(homeB)
//                .awayR(awayR).awayH(awayH).awayE(awayE).awayB(awayB)
//                .winPitcher(winPitcher)
//                .losePitcher(losePitcher)
//                .build();
//
//        scoreBoardService.saveOrUpdate(scoreBoard);
//    }
//
//    private void saveGameHighlights(Document doc, Integer scheduleId) {
//        Element highlightBox = doc.selectFirst("div.sh_box:has(.box_head:contains(결정적 장면 Best 5)) table");
//        if (highlightBox == null) {
//            System.out.println("결정적 장면 테이블 없음");
//            return;
//        }
//
//        Elements rows = highlightBox.select("tbody > tr");
//        int ranking = 1;
//
//        for (Element row : rows) {
//            Elements tds = row.select("td");
//
//            GameHighlightDTO dto = GameHighlightDTO.builder()
//                    .scheduleId(scheduleId)
//                    .ranking(ranking++)
//                    .inning(tds.get(0).text().trim())
//                    .pitcherName(tds.get(1).text().trim())
//                    .batterName(tds.get(2).text().trim())
//                    .pitchCount(tds.get(3).text().trim())
//                    .result(tds.get(4).text().trim())
//                    .beforeSituation(tds.get(5).text().trim())
//                    .afterSituation(tds.get(6).text().trim())
//                    .build();
//
//            gameHighlightService.saveOrUpdate(dto);
//        }
//    }
//
//    private void parseScores(Elements tds, List<Integer> scoreList) {
//        for (int i = 1; i <= 9; i++) {
//            String score = Optional.ofNullable(tds.get(i).selectFirst(".score"))
//                    .map(Element::ownText).orElse("0");
//            scoreList.add(Integer.parseInt(score.trim()));
//        }
//    }
//
//    private int getInt(Element td) {
//        return Integer.parseInt(td.selectFirst(".score").ownText().trim());
//    }
//
//    private String toInningString(List<Integer> scores) {
//        return String.join(" ", scores.subList(0, 9).stream()
//                .map(String::valueOf)
//                .toArray(String[]::new));
//    }
//
//    private String extractPitcher(Document doc, String selector) {
//        Element element = doc.selectFirst(selector);
//        return element != null ? element.text() : null;
//    }
//}
