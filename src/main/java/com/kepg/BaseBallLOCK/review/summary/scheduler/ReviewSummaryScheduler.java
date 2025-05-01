package com.kepg.BaseBallLOCK.review.summary.scheduler;

import java.time.LocalDate;
import java.time.DayOfWeek;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kepg.BaseBallLOCK.review.summary.service.ReviewSummaryService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewSummaryScheduler {

    private final ReviewSummaryService reviewSummaryService;

    @Scheduled(cron = "0 0 0 * * MON")
    public void generateWeeklySummariesForAllUsers() {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        reviewSummaryService.generateWeeklySummaryForAllUsers(weekStart);
    }
}