package com.kepg.BaseBallLOCK.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StatizePitcherCrawler {
	public static void main(String[] args) {
		String baseUrl = "https://statiz.sporki.com/stats/?m=main&m2=pitching&m3=default&so=WAR&ob=DESC"
	               + "&year=%d&lt=10100&reg=A";  // year만 바뀜 

		int[] years = {2020, 2021, 2022, 2023, 2024, 2025};

		for (int year : years) {
		    String url = String.format(baseUrl, year);
		    System.out.println("투수 크롤링 시작: " + year);
		    
		    try {
		        Document doc = Jsoup.connect(url)
		                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123 Safari/537.36")
		                .referrer("https://www.google.com")
		                .timeout(10000)
		                .get();

		        Element table = doc.selectFirst("div.table_type01 > table");
		        if (table == null) {
		            System.out.println("❌ 테이블 없음 (year: " + year + ")");
		            continue;
		        }

		        Elements rows = table.select("tr");
		        for (Element row : rows) {
		            Elements cols = row.select("td");
		            if (cols.size() > 10) {
		                String name = cols.get(1).text();
		                String seasonPosition = cols.get(2).text().trim();
	                     String[] sp = seasonPosition.split(" ");
	                     String season = "";
	                     if (sp.length >= 1) season = sp[0];

		                String games = cols.get(4).text(); // 경기수
		                String WINS = cols.get(10).text(); // 승리
		                String DEFEATS = cols.get(11).text(); // 패배
		                String SAVES = cols.get(12).text(); // 세이브
		                String HOLDS = cols.get(13).text(); // 홀드
		                String IP = cols.get(14).text(); // 이닝
		                String HITS = cols.get(19).text(); // 피안타
		                String HR = cols.get(22).text(); // 피홈런
		                String SO = cols.get(26).text(); // 삼진아웃
		                String BB = cols.get(23).text(); // 볼넷
		                String ERA = cols.get(30).text(); // 평균 자책점
		                String WHIP = cols.get(35).text(); // 이닝당 출루 허용율
		                String WAR = cols.get(36).text(); // 대체 선수 대비 승리기여도

		                System.out.println("==============================================");
		                System.out.println("이름: " + name + " / 시즌: " + season);
		                System.out.println("경기수: " + games + " / 승: " + WINS + " / 패: " + DEFEATS + " / 세: " + SAVES + " / 홀드: " + HOLDS);
		                System.out.println("이닝: " + IP + " / 피안타: " + HITS + " / 피홈런: " + HR);
		                System.out.println("삼진: " + SO + " / 볼넷: " + BB);
		                System.out.println("ERA: " + ERA + " / WHIP: " + WHIP + " / WAR: " + WAR);
		            }
		        }

		        Thread.sleep((int)(1000 + Math.random() * 2000));

		    } catch (Exception e) {
		        System.out.println("⚠️ 에러 (year: " + year + ")");
		        e.printStackTrace();
		    }
		}
    }
}
