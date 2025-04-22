package com.kepg.BaseBallLOCK.game.schedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kepg.BaseBallLOCK.game.schedule.dto.ScheduleCardView;
import com.kepg.BaseBallLOCK.game.schedule.service.ScheduleService;

import lombok.RequiredArgsConstructor;

@RequestMapping("/schedule")
@Controller
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/result-view")
    public String resultView(@RequestParam(required = false) Integer year,
                              @RequestParam(required = false) Integer month,
                              Model model) {
    	LocalDate today = LocalDate.now();

	    int selectedYear;
	    int selectedMonth;

	    if (year != null) {
	        selectedYear = year;
	    } else {
	        selectedYear = today.getYear();
	    }

	    if (month != null) {
	        selectedMonth = month;
	    } else {
	        selectedMonth = today.getMonthValue();
	    }
	    
        Map<LocalDate, List<ScheduleCardView>> groupedSchedule = scheduleService.getGroupedScheduleByMonth(selectedYear, selectedMonth);
        
        LocalDate current = LocalDate.of(selectedYear, selectedMonth, 1);
	    LocalDate prev = current.minusMonths(1);
	    LocalDate next = current.plusMonths(1);

	    model.addAttribute("year", selectedYear);
	    model.addAttribute("month", selectedMonth);
	    model.addAttribute("prevYear", prev.getYear());
	    model.addAttribute("prevMonth", prev.getMonthValue());
	    model.addAttribute("nextYear", next.getYear());
	    model.addAttribute("nextMonth", next.getMonthValue());
	    model.addAttribute("groupedSchedule", groupedSchedule);
	    
        return "schedule/result";
    }

    @GetMapping("/detail-view")
    public String gameDetail(@RequestParam("matchId") int matchId, Model model) {
        ScheduleCardView detail = scheduleService.getScheduleDetailById(matchId);
        
	    if (detail == null) {
	        return "redirect:/game/result-view";
	    }

	    model.addAttribute("game", detail);
        return "schedule/detail";
    }
}
