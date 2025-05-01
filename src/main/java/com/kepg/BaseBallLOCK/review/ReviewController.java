package com.kepg.BaseBallLOCK.review;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kepg.BaseBallLOCK.game.schedule.dto.GameDetailCardView;
import com.kepg.BaseBallLOCK.game.schedule.dto.ScheduleCardView;
import com.kepg.BaseBallLOCK.game.schedule.service.ScheduleService;
import com.kepg.BaseBallLOCK.review.domain.Review;
import com.kepg.BaseBallLOCK.review.domain.ReviewSummary;
import com.kepg.BaseBallLOCK.review.dto.CalendarDayDTO;
import com.kepg.BaseBallLOCK.review.service.ReviewService;
import com.kepg.BaseBallLOCK.review.summary.service.ReviewSummaryService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/review")
public class ReviewController {

    private final ReviewService reviewService;
    private final ScheduleService scheduleService;
    private final ReviewSummaryService reviewSummaryService;

    @GetMapping("/calendar-view")
    public String reviewCalendar(@RequestParam(value = "year", required = false) Integer year,
                                  @RequestParam(value = "month", required = false) Integer month,
                                  HttpSession session,
                                  Model model) {
    	
        int userId = (Integer) session.getAttribute("userId");
        Integer myTeamId = (Integer) session.getAttribute("favoriteTeamId");
        if (myTeamId == null) {
            myTeamId = 999;
        }

        // 오늘 날짜 → 이번 주 시작일 (월요일)
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        
        int currentYear = (year != null) ? year : today.getYear();
        int currentMonth = (month != null) ? month : today.getMonthValue();

        YearMonth currentYearMonth = YearMonth.of(currentYear, currentMonth);
        YearMonth prevMonth = currentYearMonth.minusMonths(1);
        YearMonth nextMonth = currentYearMonth.plusMonths(1);

        List<List<CalendarDayDTO>> calendar = reviewService.generateCalendar(currentYear, currentMonth, userId, myTeamId);
        String teamColor = reviewService.getTeamColorByTeamId(myTeamId);

        // 요약 자동 생성 시도
        ReviewSummary summary = reviewSummaryService.getWeeklySummaryByStartDate(userId, weekStart);
        if (summary == null) {
        	reviewSummaryService.generateWeeklyReviewSummary(userId, weekStart);
            summary = reviewSummaryService.getWeeklySummaryByStartDate(userId, weekStart);
        }
        
        Map<LocalDate, Boolean> summaryExistMap = new HashMap<>();
        for (List<CalendarDayDTO> week : calendar) {
            if (week.get(0) != null) {
                LocalDate date = week.get(0).getDate();
                boolean exists = reviewSummaryService.summaryExistsForWeek(userId, date);
                summaryExistMap.put(date, exists);
            }
        }
        
        model.addAttribute("summaryExistMap", summaryExistMap);
        model.addAttribute("year", currentYear);
        model.addAttribute("month", currentMonth);
        model.addAttribute("prevYear", prevMonth.getYear());
        model.addAttribute("prevMonth", prevMonth.getMonthValue());
        model.addAttribute("nextYear", nextMonth.getYear());
        model.addAttribute("nextMonth", nextMonth.getMonthValue());
        model.addAttribute("calendar", calendar);
        model.addAttribute("myTeamId", myTeamId);
        model.addAttribute("teamColor", teamColor);
        model.addAttribute("today", today);

        return "review/home";
    }
    
    @GetMapping("/write-view")
    public String writeReviewPage(@RequestParam(value = "scheduleId", required = false) Integer scheduleId,
            @RequestParam(value = "reviewId", required = false) Integer reviewId,
    		HttpSession session,
    		Model model) {
        Integer myTeamId = (Integer) session.getAttribute("favoriteTeamId");
        if (myTeamId == null) {
            myTeamId = 999;
        }
        int userId = (Integer) session.getAttribute("userId");
        
        if (scheduleId == null) {
            return "redirect:/review/calendar-view";
        }
        
        // 경기 상세 정보 가져오기
        ScheduleCardView scheduleInfo = scheduleService.getScheduleDetailById(scheduleId);
        
        // teamColor 가져오기
        String teamColor = reviewService.getTeamColorByTeamId(myTeamId);
        
        // game 정보 가져오기 (detail 화면에서 쓰는 구조 그대로)
        GameDetailCardView game = scheduleService.getGameDetail(scheduleId);
        
        // 기존 리뷰 있으면 수정용
        if (reviewId != null) {
            Optional<Review> reviewOpt = reviewService.findById(reviewId);
            System.out.println("🔍 리뷰 내용: " + reviewOpt.get().getSummary());
            if (reviewOpt.isPresent()) {
                model.addAttribute("review", reviewOpt.get());
                System.out.println("🔍 리뷰 내용: " + reviewOpt.get().getSummary());
            }
        } else {
            // 새 리뷰 객체
            Review newReview = new Review();
            newReview.setScheduleId(scheduleId);
            newReview.setUserId(userId);
            model.addAttribute("review", newReview);
        }
        model.addAttribute("teamColor", teamColor);
        model.addAttribute("scheduleInfo", scheduleInfo);
        model.addAttribute("game", game);
        
        return "review/write";
    }
    
    @GetMapping("/summary-view")
    public String reviewSummaryView(@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                    HttpSession session, Model model) {

        LocalDate weekStart = startDate.with(DayOfWeek.MONDAY);

        int userId = (Integer) session.getAttribute("userId");
        Integer teamId = (Integer) session.getAttribute("favoriteTeamId");
        if (teamId == null) teamId = 999;
        
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        List<List<CalendarDayDTO>> calendar = reviewService.generateCalendar(year, month, userId, teamId);

        // 주간 요약 조회 시도
        ReviewSummary summary = reviewSummaryService.getWeeklySummaryByStartDate(userId, weekStart);

        if (summary == null) {
            reviewSummaryService.generateWeeklyReviewSummary(userId, weekStart);
            summary = reviewSummaryService.getWeeklySummaryByStartDate(userId, weekStart);
        }

        Map<LocalDate, Boolean> summaryExistMap = new HashMap<>();
        for (List<CalendarDayDTO> week : calendar) {
            if (week.get(0) != null) {
                LocalDate date = week.get(0).getDate();
                boolean exists = reviewSummaryService.summaryExistsForWeek(userId, date);
                summaryExistMap.put(date, exists);
            }
        }

        model.addAttribute("calendar", calendar);
        model.addAttribute("summaryExistMap", summaryExistMap);
        model.addAttribute("summary", summary);
        model.addAttribute("teamColor", reviewService.getTeamColorByTeamId(teamId));
        model.addAttribute("teamId", teamId);

        return "review/summary";
    }

}