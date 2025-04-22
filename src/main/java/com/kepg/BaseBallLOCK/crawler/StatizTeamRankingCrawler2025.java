package com.kepg.BaseBallLOCK.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.team.teamRanking.domain.TeamRanking;
import com.kepg.BaseBallLOCK.team.teamRanking.repository.TeamRankingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizTeamRankingCrawler2025 {

    private final TeamRankingRepository teamRankingRepository;

    private static final Map<String, Integer> teamNameToId = new HashMap<>();
    static {
        teamNameToId.put("KIA", 1);
        teamNameToId.put("두산", 2);
        teamNameToId.put("삼성", 3);
        teamNameToId.put("SSG", 4);
        teamNameToId.put("SK", 4);
        teamNameToId.put("LG", 5);
        teamNameToId.put("한화", 6);
        teamNameToId.put("NC", 7);
        teamNameToId.put("KT", 8);
        teamNameToId.put("롯데", 9);
        teamNameToId.put("키움", 10);
    }

    // 매일 00시 정각에 자동 실행
    @Scheduled(cron = "0 15 0 * * *")
    public void runScheduledCrawling() {
        crawlTeamRankings();
    }

    public void crawlTeamRankings() {
        String baseUrl = "https://statiz.sporki.com/season/?m=teamoverall&year=%d";
        int[] years = {2025};

        for (int year : years) {
            System.out.println("시즌 " + year + " 팀 Ranking 데이터수집 시작");
            String url = String.format(baseUrl, year);
            System.out.println("크롤링 대상 URL: " + url);

            try {
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .referrer("https://www.google.com")
                        .timeout(10000)
                        .get();

                Elements tables = doc.select("div.item_box table");
                if (tables.size() < 2) {
                    System.out.println("정규시즌 중 순위 테이블이 없습니다.");
                    continue;
                }

                Element rankingTable = tables.get(1);
                Elements rows = rankingTable.select("tbody > tr");

                for (Element row : rows) {
                    Elements cols = row.select("td");
                    if (cols.size() < 8) continue;

                    String teamName = cols.get(1).text().trim();
                    int teamId = teamNameToId.getOrDefault(teamName, 0);
                    if (teamId == 0) continue;

                    int ranking = Integer.parseInt(cols.get(0).text().trim());
                    int games = Integer.parseInt(cols.get(2).text().trim());
                    int wins = Integer.parseInt(cols.get(3).text().trim());
                    int draws = Integer.parseInt(cols.get(4).text().trim());
                    int losses = Integer.parseInt(cols.get(5).text().trim());

                    String gbText = cols.get(6).text().trim();
                    double gamesBehind = gbText.equals("-") ? 0.0 : Double.parseDouble(gbText);
                    double winRate = Double.parseDouble(cols.get(7).text().trim());

                    TeamRanking entity = teamRankingRepository.findBySeasonAndTeamId(year, teamId)
                            .map(existing -> {
                                existing.setRanking(ranking);
                                existing.setGames(games);
                                existing.setWins(wins);
                                existing.setDraws(draws);
                                existing.setLosses(losses);
                                existing.setGamesBehind(gamesBehind);
                                existing.setWinRate(winRate);
                                return existing;
                            })
                            .orElse(TeamRanking.builder()
                                    .season(year)
                                    .teamId(teamId)
                                    .ranking(ranking)
                                    .games(games)
                                    .wins(wins)
                                    .draws(draws)
                                    .losses(losses)
                                    .gamesBehind(gamesBehind)
                                    .winRate(winRate)
                                    .build());

                    teamRankingRepository.save(entity);
                    System.out.printf("저장 완료 - 시즌 %d, 팀 %s (%d위)\n", year, teamName, ranking);
                }

            } catch (Exception e) {
                System.out.println("크롤링 실패: year=" + year);
                e.printStackTrace();
            }
        }
    }
}