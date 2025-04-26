//package com.kepg.BaseBallLOCK.crawler;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.kepg.BaseBallLOCK.game.scoreBoard.domain.ScoreBoard;
//import com.kepg.BaseBallLOCK.game.scoreBoard.service.ScoreBoardService;
//
//import lombok.RequiredArgsConstructor;
//
//@Component
//@RequiredArgsConstructor
//public class StatizGameSummaryCrawlerToday {
//
//    private final ScoreBoardService scoreBoardService;
//
//    @Scheduled(cron = "0 7 0 * * *")
//    public void crawl() {
//        String baseUrl = "https://statiz.sporki.com/schedule/?m=summary&s_no=%d";
//
//        LocalDate targetDate = LocalDate.now().minusDays(1);
//        String formatted = targetDate.format(DateTimeFormatter.ofPattern("MMdd"));
//        int startId = Integer.parseInt("2025" + formatted); // e.g., 20250326
//        int endId = startId + 9;
//
//        for (int statizId = startId; statizId <= endId; statizId++) {
//            try {
//                String url = String.format(baseUrl, statizId);
//                Document doc = Jsoup.connect(url)
//                        .userAgent("Mozilla/5.0")
//                        .timeout(10000)
//                        .get();
//
//                Elements tables = doc.select(".table_type03 table");
//                if (tables.size() < 1) {
//                    continue;
//                }
//
//                Element table = tables.get(0);
//                Elements rows = table.select("tbody > tr");
//                if (rows.size() < 2) {
//                    continue;
//                }
//
//                List<Integer> homeScores = new ArrayList<>();
//                List<Integer> awayScores = new ArrayList<>();
//
//                Element awayRow = rows.get(0);
//                Elements awayTds = awayRow.select("td");
//                String awayTeam = "";
//                Element awayTeamElement = awayTds.get(0);
//                if (awayTeamElement != null) {
//                    awayTeam = awayTeamElement.text();
//                }
//
//                for (int i = 1; i <= 9; i++) {
//                    Element scoreDiv = awayTds.get(i).selectFirst(".score");
//                    String scoreText = scoreDiv != null ? scoreDiv.ownText().trim() : "0";
//                    awayScores.add(Integer.parseInt(scoreText));
//                }
//
//                int awayR = Integer.parseInt(awayTds.get(13).selectFirst(".score").ownText().trim());
//                int awayH = Integer.parseInt(awayTds.get(14).selectFirst(".score").ownText().trim());
//                int awayE = Integer.parseInt(awayTds.get(15).selectFirst(".score").ownText().trim());
//                int awayB = Integer.parseInt(awayTds.get(16).selectFirst(".score").ownText().trim());
//
//                Element homeRow = rows.get(1);
//                Elements homeTds = homeRow.select("td");
//                String homeTeam = "";
//                Element homeTeamElement = homeTds.get(0);
//                if (homeTeamElement != null) {
//                    homeTeam = homeTeamElement.text();
//                }
//
//                for (int i = 1; i <= 9; i++) {
//                    Element scoreDiv = homeTds.get(i).selectFirst(".score");
//                    String scoreText = scoreDiv != null ? scoreDiv.ownText().trim() : "0";
//                    homeScores.add(Integer.parseInt(scoreText));
//                }
//
//                int homeR = Integer.parseInt(homeTds.get(13).selectFirst(".score").ownText().trim());
//                int homeH = Integer.parseInt(homeTds.get(14).selectFirst(".score").ownText().trim());
//                int homeE = Integer.parseInt(homeTds.get(15).selectFirst(".score").ownText().trim());
//                int homeB = Integer.parseInt(homeTds.get(16).selectFirst(".score").ownText().trim());
//
//                String homeInning = homeScores.stream().map(String::valueOf).collect(Collectors.joining(" "));
//                String awayInning = awayScores.stream().map(String::valueOf).collect(Collectors.joining(" "));
//
//                String winPitcher = null;
//                Element winAnchor = doc.selectFirst(".game_result .win a");
//                if (winAnchor != null) {
//                    winPitcher = winAnchor.text();
//                }
//
//                String losePitcher = null;
//                Element loseAnchor = doc.selectFirst(".game_result .lose a");
//                if (loseAnchor != null) {
//                    losePitcher = loseAnchor.text();
//                }
//
//                int scheduleId = optionalScheduleId;
//
//                ScoreBoard scoreBoard = ScoreBoard.builder()
//                      .scheduleId(scheduleId)
//                      .homeScore(homeR)
//                      .awayScore(awayR)
//                      .homeInningScores(homeInning)
//                      .awayInningScores(awayInning)
//                      .homeR(homeR).homeH(homeH).homeE(homeE).homeB(homeB)
//                      .awayR(awayR).awayH(awayH).awayE(awayE).awayB(awayB)
//                      .winPitcher(winPitcher)
//                      .losePitcher(losePitcher)
//                      .build();
//
//                scoreBoardService.saveOrUpdate(scoreBoard);
//              
//                System.out.println("[저장 완료] statizId=" + statizId + " " + homeTeam + " vs " + awayTeam);
//
//                Thread.sleep(2000);
//            } catch (Exception e) {
//                System.out.println("오류 발생: " + statizId);
//                e.printStackTrace();
//            }
//        }
//    }
//}