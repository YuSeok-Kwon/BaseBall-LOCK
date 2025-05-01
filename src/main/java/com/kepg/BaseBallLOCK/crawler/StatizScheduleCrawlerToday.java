package com.kepg.BaseBallLOCK.crawler;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

import com.kepg.BaseBallLOCK.game.schedule.domain.Schedule;
import com.kepg.BaseBallLOCK.game.schedule.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizScheduleCrawlerToday {

    private final ScheduleService scheduleService;

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

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void crawl() {
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

                Elements teamSpans = gameDoc.select("div.score_box .team");
                Elements scoreSpans = gameDoc.select("div.score_box .score");
                Element rainSpan = gameDoc.selectFirst(".score_box .weather.rain");

                if (teamSpans.size() != 2) continue;

                String awayTeam = teamSpans.get(0).text().trim();
                String homeTeam = teamSpans.get(1).text().trim();

                int[] teamIds = new int[2];
                if (!parseTeamIds(homeTeam, awayTeam, teamIds)) continue;
                int homeTeamId = teamIds[0];
                int awayTeamId = teamIds[1];

                int[] scores = new int[2];
                String status = determineMatchStatus(rainSpan, scoreSpans, scores);
                Integer homeScore = (status.equals("종료")) ? scores[0] : null;
                Integer awayScore = (status.equals("종료")) ? scores[1] : null;

                String matchDateStr = yesterday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                saveSchedule(matchDate, homeTeamId, awayTeamId,
                        homeScore, awayScore, status,
                        teamIdToStadium.get(homeTeamId),
                        matchDateStr, homeTeam, awayTeam);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
    
    private Timestamp extractMatchDate(Document doc) {
        try {
            Element calloutBox = doc.selectFirst(".callout_box .txt");
            if (calloutBox == null) return null;
            String[] parts = calloutBox.text().split(",");
            String mmdd = parts[1].trim(); // 예: "04-30"
            LocalDate date = LocalDate.parse("2025-" + mmdd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return Timestamp.valueOf(date.atStartOfDay());
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean parseTeamIds(String homeTeam, String awayTeam, int[] teamIds) {
        Integer homeTeamId = teamNameToId.getOrDefault(homeTeam, 0);
        Integer awayTeamId = teamNameToId.getOrDefault(awayTeam, 0);

        if (homeTeamId == 0 || awayTeamId == 0) return false;

        teamIds[0] = homeTeamId;
        teamIds[1] = awayTeamId;
        return true;
    }

    private String determineMatchStatus(Element rainSpan, Elements scoreSpans, int[] scores) {
        if (rainSpan != null) {
            return "우천취소";
        } else if (scoreSpans.size() == 2) {
            scores[0] = tryParseInt(scoreSpans.get(1).text().trim()); // home
            scores[1] = tryParseInt(scoreSpans.get(0).text().trim()); // away
            return "종료";
        }
        return "예정";
    }

    private void saveSchedule(Timestamp matchDate, int homeTeamId, int awayTeamId,
                              Integer homeScore, Integer awayScore, String status,
                              String stadium, String matchDateStr, String homeTeam, String awayTeam) {
        Schedule schedule = Schedule.builder()
                .matchDate(matchDate)
                .homeTeamId(homeTeamId)
                .awayTeamId(awayTeamId)
                .homeTeamScore(homeScore)
                .awayTeamScore(awayScore)
                .status(status)
                .stadium(stadium)
                .build();

        scheduleService.saveOrUpdate(schedule);

        System.out.printf("저장 완료: %s - %s(%s) vs %s(%s) [%s]\n",
                matchDateStr,
                homeTeam, homeScore != null ? homeScore : "-",
                awayTeam, awayScore != null ? awayScore : "-",
                status);
    }
    
    private Integer tryParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }
}