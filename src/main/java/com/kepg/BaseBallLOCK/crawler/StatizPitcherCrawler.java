package com.kepg.BaseBallLOCK.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.player.domain.Player;
import com.kepg.BaseBallLOCK.player.dto.PlayerDTO;
import com.kepg.BaseBallLOCK.player.service.PlayerService;
import com.kepg.BaseBallLOCK.player.stats.service.PitcherStatsService;
import com.kepg.BaseBallLOCK.player.stats.statsDto.PitcherStatsDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StatizPitcherCrawler {

    private final PlayerService playerService;
    private final PitcherStatsService statsService;

    private static final Map<String, Integer> colorToTeamIdMap = new HashMap<>();
    static {
        colorToTeamIdMap.put("#042071", 2); // 두산
        colorToTeamIdMap.put("#cf152d", 4); // SSG
        colorToTeamIdMap.put("#ff0000", 4); // SSG + SK
        colorToTeamIdMap.put("#ed1c24", 1); // KIA
        colorToTeamIdMap.put("#002b69", 7); // NC
        colorToTeamIdMap.put("#000000", 8); // KT
        colorToTeamIdMap.put("#888888", 9); // 롯데
        colorToTeamIdMap.put("#f37321", 6); // 한화
        colorToTeamIdMap.put("#0061AA", 3); // 삼성
        colorToTeamIdMap.put("#fc1cad", 5); // LG
        colorToTeamIdMap.put("#86001f", 10); // 키움
    }

    public void crawl() {
        String[] urlPatterns = {
            "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC&year=%d&lt=10100&reg=A",
            "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=ASC&year=%d&sy=&ey=&te=&po=&lt=10100&reg=A&pe=&ds=&de=&we=&hr=&ha=&ct=&st=&vp=&bo=&pt=&pp=&ii=&vc=&um=&oo=&rr=&sc=&bc=&ba=&li=&as=&ae=&pl=&gc=&lr=&pr=50&ph=&hs=&us=&na=&ls=&sf1=&sk1=&sv1=&sf2=&sk2=&sv2="
        };

        int[] years = {2020};

        for (String urlPattern : urlPatterns) {
            for (int year : years) {
                String url = String.format(urlPattern, year);
                System.out.println("투수 크롤링: year=" + year);

                try {
                    Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123 Safari/537.36")
                        .referrer("https://www.google.com")
                        .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        .header("Connection", "keep-alive")
                        .timeout(70000)
                        .get();

                    Element table = doc.selectFirst("table");
                    if (table == null) continue;

                    for (Element row : table.select("tr")) {
                        Elements cols = row.select("td");
                        if (cols.size() <= 36) continue;

                        String name = cols.get(1).text().trim();
                        String[] sp = cols.get(2).text().trim().split(" ");
                        int season = Integer.parseInt(sp[0]);
                        String position = "P";

                        Element colorElem = cols.get(2).selectFirst("span[style]");
                        int teamId = 0;
                        if (colorElem != null) {
                            String bg = colorElem.attr("style").split("background:")[1].split(";")[0].trim();
                            teamId = colorToTeamIdMap.getOrDefault(bg, 0);
                        }

                        PlayerDTO dto = PlayerDTO.builder()
                            .name(name)
                            .teamId(teamId)
                            .build();
                        Player player = playerService.findOrCreatePlayer(dto);
                        int playerId = player.getId();

                        save(playerId, season, "G", parseInt(cols.get(4).text()), null, position);
                        save(playerId, season, "W", parseInt(cols.get(10).text()), null, position);
                        save(playerId, season, "L", parseInt(cols.get(11).text()), null, position);
                        save(playerId, season, "SV", parseInt(cols.get(12).text()), null, position);
                        save(playerId, season, "HLD", parseInt(cols.get(13).text()), null, position);
                        save(playerId, season, "IP", parseDouble(cols.get(14).text()), null, position);
                        save(playerId, season, "H", parseInt(cols.get(19).text()), null, position);
                        save(playerId, season, "HR", parseInt(cols.get(22).text()), null, position);
                        save(playerId, season, "BB", parseInt(cols.get(23).text()), null, position);
                        save(playerId, season, "SO", parseInt(cols.get(26).text()), null, position);
                        save(playerId, season, "ERA", parseDouble(cols.get(30).text()), null, position);
                        save(playerId, season, "WHIP", parseDouble(cols.get(35).text()), null, position);
                        save(playerId, season, "WAR", parseDouble(cols.get(36).text()), null, position);
                    }

                } catch (Exception e) {
                    System.out.println("에러 발생: year=" + year);
                    e.printStackTrace();
                }
            }
        }
    }

    private void save(int playerId, int season, String category, double value, Integer ranking, String position) {
        PitcherStatsDTO dto = PitcherStatsDTO.builder()
                .playerId(playerId)
                .season(season)
                .category(category)
                .value(value)
                .ranking(ranking)
                .position(position)
                .build();
        statsService.savePitcherStats(dto);
    }

    private int parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
