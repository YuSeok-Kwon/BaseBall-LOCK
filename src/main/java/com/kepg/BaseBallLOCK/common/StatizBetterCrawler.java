package com.kepg.BaseBallLOCK.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class StatizBetterCrawler {

    public static void main(String[] args) {
    	String baseUrl = "https://statiz.sporki.com/stats/?m=main&m2=batting&m3=default&so=WAR&ob=DESC"
                + "&year=%d&po=%d&lt=10100&reg=A"; // year와 po만 바뀜

		 int[] years = {2020, 2021, 2022, 2023, 2024, 2025};
		 int[] positions = {2, 3, 4, 5, 6, 7, 8, 9, 10}; // 포수~지명타자
		
		 for (int year : years) {
		     for (int po : positions) {
		         String url = String.format(baseUrl, year, po);
		         System.out.println("크롤링 시작: " + year + " / 포지션 코드: " + po);
		
		         try {
		        	 Document doc = Jsoup.connect(url)
		        			    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/123 Safari/537.36")
		        			    .referrer("https://www.google.com")
		        			    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
		        			    .header("Accept-Language", "en-US,en;q=0.5")
		        			    .header("Connection", "keep-alive")
		        			    .timeout(10000)
		        			    .get();
		
		             Element table = doc.selectFirst("div.table_type01 > table");
		
		             if (table == null) {
		                 System.out.println("❌ 테이블 없음 (year: " + year + ", po: " + po + ")");
		                 continue;
		             }
		
		             Elements rows = table.select("tr");
		
		             for (Element row : rows) {
		                 Elements cols = row.select("td");
		                 if (cols.size() > 31) {
		                     String playerName = cols.get(1).text().trim();
		
		                     String seasonPosition = cols.get(2).text().trim();
		                     String[] sp = seasonPosition.split(" ");
		                     String season = "";
		                     String position = "";
		                     if (sp.length >= 1) season = sp[0];
		                     if (sp.length >= 2) position = sp[1];
		
		                     String games = cols.get(4).text();
		                     String PA = cols.get(7).text();
		                     String WAR = cols.get(3).text();
		                     String HITS = cols.get(11).text();
		                     String DOUBLE = cols.get(12).text();
		                     String TRIPLE = cols.get(13).text();
		                     String HR = cols.get(14).text();
		                     String RBI = cols.get(16).text();
		                     String SB = cols.get(17).text();
		                     String BB = cols.get(19).text();
		                     String SO = cols.get(22).text();
		                     String AVG = cols.get(26).text();
		                     String OBP = cols.get(27).text();
		                     String SLG = cols.get(28).text();
		                     String OPS = cols.get(29).text();
		                     String wRCplus = cols.get(31).text();
		
		                     // 콘솔 출력 확인
		                     System.out.println("==============================================");
		                     System.out.println("이름: " + playerName);
		                     System.out.println("시즌: " + season + " / 포지션: " + position);
		                     System.out.println("WAR: " + WAR + " / 경기: " + games + " / 타석: " + PA);
		                     System.out.println("안타: " + HITS + " / 2루타: " + DOUBLE + " / 3루타: " + TRIPLE + " / 홈런: " + HR + " / 타점: " + RBI);
		                     System.out.println("도루: " + SB + " / 볼넷: " + BB + " / 삼진: " + SO);
		                     System.out.println("AVG: " + AVG + " / 출루율: " + OBP + " / 장타율: " + SLG + " / OPS: " + OPS);
		                     System.out.println("wRC+: " + wRCplus);
		                 }
		             }
		             Thread.sleep((int)(1000 + Math.random() * 2000));
		             
		         } catch (Exception e) {
		             System.out.println("⚠️ 에러 (year: " + year + ", po: " + po + ")");
		             e.printStackTrace();
		         }
		     }
		 }
    }
}
