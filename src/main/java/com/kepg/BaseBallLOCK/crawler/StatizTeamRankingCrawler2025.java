package com.kepg.BaseBallLOCK.crawler;

import com.kepg.BaseBallLOCK.team.teamRanking.teamRankingDao.TeamRankingDAO;

import com.kepg.BaseBallLOCK.team.teamRanking.teamRankingDto.TeamRankingDTO;
import jakarta.annotation.PostConstruct;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Component
public class StatizTeamRankingCrawler2025 {

    @Autowired
    private DataSource dataSource;

    private Map<String, Integer> teamNameToId;

    @PostConstruct
    public void initTeamMap() {
        teamNameToId = new HashMap<>();
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

    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00 (자정)
    public void updateTeamRanking() {
        String url = "https://statiz.sporki.com/season/?m=teamoverall&year=2025";

        try (Connection conn = dataSource.getConnection()) {
            TeamRankingDAO teamRankingDAO = new TeamRankingDAO(conn);

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .referrer("https://www.google.com")
                    .timeout(10000)
                    .get();

            Element table = doc.select("div.item_box table").get(0); // 첫 번째 테이블
            Elements rows = table.select("tbody > tr");

            for (Element row : rows) {
                Elements cols = row.select("td");
                if (cols.size() < 8) continue;

                String teamName = cols.get(1).text().trim();
                int teamId = teamNameToId.getOrDefault(teamName, 0);
                if (teamId == 0) continue;

                int season = 2025;
                int rank = Integer.parseInt(cols.get(0).text());
                int g = Integer.parseInt(cols.get(2).text());
                int w = Integer.parseInt(cols.get(3).text());
                int d = Integer.parseInt(cols.get(4).text());
                int l = Integer.parseInt(cols.get(5).text());
                double gb = Double.parseDouble(cols.get(6).text());
                double winRate = Double.parseDouble(cols.get(7).text());

                TeamRankingDTO dto = new TeamRankingDTO();
                dto.setSeason(season);
                dto.setTeamId(teamId);
                dto.setRanking(rank);
                dto.setGames(g);
                dto.setWins(w);
                dto.setDraws(d);
                dto.setLosses(l);
                dto.setGamesBehind(gb);
                dto.setWinRate(winRate);

                teamRankingDAO.saveOrUpdate(dto);
                System.out.printf("저장 완료 - 팀: %s (%d위)\n", teamName, rank);
            }

        } catch (Exception e) {
            System.out.println("에러 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}