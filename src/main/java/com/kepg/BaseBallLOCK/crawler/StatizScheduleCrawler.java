package com.kepg.BaseBallLOCK.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.kepg.BaseBallLOCK.game.schedule.scheduleDao.ScheduleDAO;
import com.kepg.BaseBallLOCK.game.schedule.scheduleDto.ScheduleDTO;

public class StatizScheduleCrawler {

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
        teamIdToStadium.put(8, "수원 케이티위즈파크");
        teamIdToStadium.put(9, "부산 사직야구장");
        teamIdToStadium.put(10, "서울 고척스카이돔");
    }

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://localhost:3306/BaseBallLOCK?serverTimezone=Asia/Seoul";
        String username = "root";
        String password = "rnjs7944";

        String baseUrl = "https://statiz.sporki.com/schedule/?year=%d&month=%d";

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            ScheduleDAO scheduleDAO = new ScheduleDAO(conn);

            for (int year = 2025; year <= 2025; year++) {
                for (int month = 3; month <= 9; month++) {
                    String url = String.format(baseUrl, year, month);
                    System.out.println("크롤링 시작: " + url);

                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0")
                            .referrer("https://www.google.com")
                            .timeout(10000)
                            .get();

                    Elements tdElements = doc.select("div.calendar_area td");

                    for (Element td : tdElements) {
                        Element dayElement = td.selectFirst("span.day");
                        if (dayElement == null) continue;

                        String day = dayElement.text().trim();
                        if (day.isEmpty()) continue;

                        String matchDateStr = String.format("%d%02d%02d", year, month, Integer.parseInt(day));
                        LocalDate localDate = LocalDate.parse(matchDateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
                        Timestamp matchDate = Timestamp.valueOf(localDate.atStartOfDay());

                        Elements gameItems = td.select("div.games li");

                        for (Element game : gameItems) {
                            Elements teamSpans = game.select("a > span.team");
                            Elements scoreSpans = game.select("a > span.score");
                            Element rainSpan = game.selectFirst("span.weather.rain");

                            if (teamSpans.size() != 2) continue;

                            String awayTeam = teamSpans.get(0).text().trim();
                            String homeTeam = teamSpans.get(1).text().trim();
                            int homeTeamId = teamNameToId.getOrDefault(homeTeam, 0);
                            int awayTeamId = teamNameToId.getOrDefault(awayTeam, 0);

                            if (homeTeamId == 0 || awayTeamId == 0) {
                                System.out.printf("알 수 없는 팀 이름: home=%s, away=%s\n", homeTeam, awayTeam);
                                continue;
                            }

                            Integer homeScore = null;
                            Integer awayScore = null;
                            String status = "예정";

                            if (rainSpan != null) {
                                status = "우천취소";
                            } else if (scoreSpans.size() == 2) {
                                homeScore = tryParseInt(scoreSpans.get(1).text().trim());
                                awayScore = tryParseInt(scoreSpans.get(0).text().trim());
                                status = "종료";
                            }

                            ScheduleDTO dto = new ScheduleDTO();
                            dto.setMatchDate(matchDate);
                            dto.setHomeTeamId(homeTeamId);
                            dto.setAwayTeamId(awayTeamId);
                            dto.setHomeTeamScore(homeScore);
                            dto.setAwayTeamScore(awayScore);
                            dto.setStadium(teamIdToStadium.get(homeTeamId));
                            dto.setStatus(status);

                            scheduleDAO.insertOrUpdateMatch(dto);
                            System.out.printf("저장 완료: %s - %s (%s) vs %s (%s) 상태: %s\n",
                                    matchDateStr, homeTeam, homeScore != null ? homeScore : "-",
                                    awayTeam, awayScore != null ? awayScore : "-", status);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Integer tryParseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return null;
        }
    }
}