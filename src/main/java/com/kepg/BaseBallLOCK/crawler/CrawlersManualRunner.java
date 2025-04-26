package com.kepg.BaseBallLOCK.crawler;

import org.springframework.boot.SpringApplication;

import org.springframework.context.ConfigurableApplicationContext;

import com.kepg.BaseBallLOCK.BaseBallLockApplication;

public class CrawlersManualRunner {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BaseBallLockApplication.class, args);

        try {
//            context.getBean(StatizBatterCrawler.class).crawl();
//            context.getBean(StatizPitcherCrawler.class).crawl();
//			  context.getBean(StatizScheduleCrawler.class).crawl();
//			  context.getBean(StatizTeamRankingCrawler.class).crawl();
//			  context.getBean(StatizTeamWaaCrawler.class).crawl();
//			  context.getBean(StatizTeamDetailedstatsCrawler.class).crawl();
//            context.getBean(StatizPlayerGameRecordCrawler.class).crawl();
        	  context.getBean(StatizGameSummaryCrawler.class).crawl();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        context.close();
    }
}
