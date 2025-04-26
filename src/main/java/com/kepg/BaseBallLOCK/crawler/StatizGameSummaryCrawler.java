package com.kepg.BaseBallLOCK.crawler;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.game.schedule.service.ScheduleService;
import com.kepg.BaseBallLOCK.game.scoreBoard.domain.ScoreBoard;
import com.kepg.BaseBallLOCK.game.scoreBoard.service.ScoreBoardService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizGameSummaryCrawler {

    private final ScoreBoardService scoreBoardService;
    private final ScheduleService scheduleService;

    private static final Map<String, Integer> teamNameToId = Map.of(
        "두산", 2,
        "SSG", 4,
        "KIA", 1,
        "NC", 7,
        "KT", 8,
        "롯데", 9,
        "한화", 6,
        "삼성", 3,
        "LG", 5,
        "키움", 10
    );

    public void crawl() {
        String baseUrl = "https://statiz.sporki.com/schedule/?m=summary&s_no=%d";

        for (int statizId = 20250001; statizId <= 20250002; statizId++) {
            try {
                String url = String.format(baseUrl, statizId);
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .referrer("https://www.google.com")
                        .header("Accept-Language", "ko-KR,ko;q=0.9")
                        .timeout(70000)
                        .get();

                Elements tables = doc.select(".table_type03 table");
                if (tables.size() < 1) {
                	continue;
                }

                Element table = tables.get(0);
                Elements rows = table.select("tbody > tr");
                if (rows.size() < 2) {
                	continue;
                }

                List<Integer> homeScores = new ArrayList<>();
                List<Integer> awayScores = new ArrayList<>();

                Element awayRow = rows.get(0);
                Elements awayTds = awayRow.select("td");
                String awayTeam = "Unknown";
                if (awayTds != null && !awayTds.isEmpty() && awayTds.get(0) != null) {
                    awayTeam = awayTds.get(0).text();
                }
                
                for (int i = 1; i <= 9; i++) {
                    String scoreText = Optional.ofNullable(awayTds.get(i))
                            .map(td -> td.selectFirst(".score"))
                            .map(Element::ownText)
                            .orElse("0")
                            .trim();
                    awayScores.add(Integer.parseInt(scoreText));
                }

                int awayR = Integer.parseInt(awayTds.get(13).selectFirst(".score").ownText().trim());
                int awayH = Integer.parseInt(awayTds.get(14).selectFirst(".score").ownText().trim());
                int awayE = Integer.parseInt(awayTds.get(15).selectFirst(".score").ownText().trim());
                int awayB = Integer.parseInt(awayTds.get(16).selectFirst(".score").ownText().trim());

                Element homeRow = rows.get(1);
                Elements homeTds = homeRow.select("td");
                String homeTeam = "Unknown";
                if (homeTds != null && !homeTds.isEmpty() && homeTds.get(0) != null) {
                    homeTeam = homeTds.get(0).text();
                }
                
                for (int i = 1; i <= 9; i++) {
                    String scoreText = Optional.ofNullable(homeTds.get(i))
                            .map(td -> td.selectFirst(".score"))
                            .map(Element::ownText)
                            .orElse("0")
                            .trim();
                    homeScores.add(Integer.parseInt(scoreText));
                }

                int homeR = Integer.parseInt(homeTds.get(13).selectFirst(".score").ownText().trim());
                int homeH = Integer.parseInt(homeTds.get(14).selectFirst(".score").ownText().trim());
                int homeE = Integer.parseInt(homeTds.get(15).selectFirst(".score").ownText().trim());
                int homeB = Integer.parseInt(homeTds.get(16).selectFirst(".score").ownText().trim());

                StringBuilder homeInningBuilder = new StringBuilder();
                for (int i = 0; i < homeScores.size(); i++) {
                    homeInningBuilder.append(homeScores.get(i));
                    if (i < homeScores.size() - 1) {
                        homeInningBuilder.append(" ");
                    }
                }
                String homeInning = homeInningBuilder.toString();

                StringBuilder awayInningBuilder = new StringBuilder();
                for (int i = 0; i < awayScores.size(); i++) {
                    awayInningBuilder.append(awayScores.get(i));
                    if (i < awayScores.size() - 1) {
                        awayInningBuilder.append(" ");
                    }
                }
                String awayInning = awayInningBuilder.toString();

				  String winPitcher = null;
				  Element winElement = doc.selectFirst(".game_result .win a");
				  if (winElement != null) {
				      winPitcher = winElement.text();
			  	  }
				
				  String losePitcher = null;
				  Element loseElement = doc.selectFirst(".game_result .lose a");
				  if (loseElement != null) {
				      losePitcher = loseElement.text();
				  }

             
                Element txtElement = doc.selectFirst(".callout_box .txt");

                String txt = txtElement.text();
                String[] parts = txt.split(",");

                String datePart = parts[1].trim();
                int year = statizId / 10000;
                String fullDate = year + "-" + datePart;

                LocalDate matchDate = LocalDate.parse(fullDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                Timestamp matchDateTime = Timestamp.valueOf(matchDate.atStartOfDay());

                int homeTeamId = teamNameToId.getOrDefault(homeTeam, 0);
                int awayTeamId = teamNameToId.getOrDefault(awayTeam, 0);

                if (homeTeamId == 0 || awayTeamId == 0) {
                    System.out.println("알 수 없는 팀: " + homeTeam + ", " + awayTeam);
                    continue;
                }

                Integer scheduleId = scheduleService.findIdByDateAndTeams(matchDateTime, homeTeamId, awayTeamId);

                if (scheduleId == null) {
                    System.out.println("Schedule ID 찾을 수 없음: statizId=" + statizId);
                    continue;
                }

                ScoreBoard scoreBoard = ScoreBoard.builder()
                        .scheduleId(scheduleId)
                        .homeScore(homeR)
                        .awayScore(awayR)
                        .homeInningScores(homeInning)
                        .awayInningScores(awayInning)
                        .homeR(homeR).homeH(homeH).homeE(homeE).homeB(homeB)
                        .awayR(awayR).awayH(awayH).awayE(awayE).awayB(awayB)
                        .winPitcher(winPitcher)
                        .losePitcher(losePitcher)
                        .build();

                System.out.println("크롤링된 ScoreBoard 데이터:");
                System.out.println(scoreBoard);

                System.out.println("출력 완료 " + statizId + " (" + scheduleId + ")" + matchDateTime);

                Thread.sleep(60000);

            } catch (Exception e) {
                System.out.println("오류 발생: " + statizId);
                e.printStackTrace();
            }
        }
    }
}
