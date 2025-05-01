package com.kepg.BaseBallLOCK.review.summary.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.game.schedule.service.ScheduleService;
import com.kepg.BaseBallLOCK.review.domain.Review;
import com.kepg.BaseBallLOCK.review.domain.ReviewSummary;
import com.kepg.BaseBallLOCK.review.repository.ReviewRepository;
import com.kepg.BaseBallLOCK.review.summary.repository.ReviewSummaryRepository;
import com.kepg.BaseBallLOCK.user.userService.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewSummaryService {
	
	private final UserService userService;
	private final ReviewRepository reviewRepository;
	private final ReviewSummaryRepository reviewSummaryRepository;
	private final AiSummaryService aiSummaryService;
	private final ScheduleService scheduleService;
	
	public ReviewSummary getWeeklySummaryByStartDate(int userId, LocalDate weekStartDate) {
        // LocalDate → java.sql.Date 변환
        Date sqlStartDate = Date.valueOf(weekStartDate);

        return reviewSummaryRepository.findByUserIdAndWeekStartDate(userId, sqlStartDate)
                .orElse(null); // 없을 경우 null 반환
    }
	
	public ReviewSummary generateWeeklyReviewSummary(Integer userId, LocalDate weekStart) {
        // 이번 주의 시작일과 종료일 계산 (월~일 기준)
        LocalDate weekEnd = weekStart.plusDays(6);

        Date weekStartDate = Date.valueOf(weekStart);
        Date weekEndDate = Date.valueOf(weekEnd);
        
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(LocalTime.MAX);

        // 주간 리뷰 데이터 조회
        List<Review> weeklyReviews = reviewRepository.findByUserIdAndScheduleMatchDateBetween(
            userId, startDateTime, endDateTime
        );

        if (weeklyReviews == null || weeklyReviews.size() < 2) {
            return null;
        }

        // 승리/패배/별점 통계 초기화
        int win = 0;
        int totalRating = 0;
        int reviewCount = weeklyReviews.size();

        // 선수 이름 → 등장 횟수 매핑
        Map<String, Integer> bestPlayerCount = new HashMap<>();
        Map<String, Integer> worstPlayerCount = new HashMap<>();

        // 날짜별 감정 리스트 저장 (감정 흐름 파악용)
        Map<LocalDate, List<String>> dailyFeelings = new TreeMap<>();

        // 리뷰 반복 처리
        for (Review review : weeklyReviews) {
            // 별점 통계
            int rating = review.getRating();
            totalRating += rating;
            if (rating >= 7) {
                win++;
            }

            // Best Player 집계
            String best = review.getBestPlayer();
            if (best != null && !best.isEmpty()) {
                if (!bestPlayerCount.containsKey(best)) {
                    bestPlayerCount.put(best, 1);
                } else {
                    int count = bestPlayerCount.get(best);
                    bestPlayerCount.put(best, count + 1);
                }
            }

            // Worst Player 집계
            String worst = review.getWorstPlayer();
            if (worst != null && !worst.isEmpty()) {
                if (!worstPlayerCount.containsKey(worst)) {
                    worstPlayerCount.put(worst, 1);
                } else {
                    int count = worstPlayerCount.get(worst);
                    worstPlayerCount.put(worst, count + 1);
                }
            }

            // 날짜별 감정 수집
            Timestamp matchTimestamp = scheduleService.findMatchDateById(review.getScheduleId());
            LocalDate matchDate = matchTimestamp.toLocalDateTime().toLocalDate();            
            dailyFeelings.computeIfAbsent(matchDate, k -> new ArrayList<>());

            String feeling = review.getFeelings();
            if (feeling != null && !feeling.trim().isEmpty()) {
                dailyFeelings.get(matchDate).add(feeling.trim());
            }
        }

        // 패배 수 계산
        int loss = reviewCount - win;

        // 평균 별점 계산
        double averageRating = (double) totalRating / reviewCount;

        // Best/Worst Player 포맷 변환 (이름 (횟수) 형태)
        String bestPlayers = formatPlayerCount(bestPlayerCount);
        String worstPlayers = formatPlayerCount(worstPlayerCount);

        // 텍스트 요약 생성
        String recordSummary = win + "승 " + loss + "패"; // ex. "1승 6패"
        String feelingSummary = aiSummaryService.summarizeFeelings(dailyFeelings); // 감정 흐름 요약
        String reviewText = aiSummaryService.generateFullSummary(weeklyReviews);   // 전체 요약 멘트
        String keywords = extractKeywords(weeklyReviews);                          // 키워드 해시태그

        // 결과 저장
        ReviewSummary summary = new ReviewSummary();
        summary.setUserId(userId);
        summary.setSeason(2025);
        summary.setWeekStartDate(weekStartDate);
        summary.setWeekEndDate(weekEndDate);
        summary.setRecordSummary(recordSummary);
        summary.setFeelingSummary(feelingSummary);
        summary.setBestMoment(bestPlayers);
        summary.setWorstMoment(worstPlayers);
        summary.setReviewText(reviewText);
        summary.setKeywords(keywords);
        summary.setTotalReviewCount(reviewCount);
        summary.setAverageRating(averageRating);
        summary.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        summary.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));

        reviewSummaryRepository.save(summary);
        
        return summary;
    }
	
	public boolean summaryExistsForWeek(int userId, LocalDate weekStartDate) {
	    Date sqlDate = Date.valueOf(weekStartDate);
	    return reviewSummaryRepository.existsByUserIdAndWeekStartDate(userId, sqlDate);
	}
	
	private String formatPlayerCount(Map<String, Integer> playerMap) {
	    // Map의 Entry들을 List로 복사
	    List<Map.Entry<String, Integer>> entryList = new ArrayList<>(playerMap.entrySet());

	    // 내림차순 정렬 (횟수가 많은 순)
	    for (int i = 0; i < entryList.size() - 1; i++) {
	        for (int j = i + 1; j < entryList.size(); j++) {
	            if (entryList.get(i).getValue() < entryList.get(j).getValue()) {
	                Map.Entry<String, Integer> temp = entryList.get(i);
	                entryList.set(i, entryList.get(j));
	                entryList.set(j, temp);
	            }
	        }
	    }

	    // "이름 (횟수회)" 형태로 문자열 변환
	    StringBuilder result = new StringBuilder();
	    for (int i = 0; i < entryList.size(); i++) {
	        Map.Entry<String, Integer> entry = entryList.get(i);
	        result.append(entry.getKey())
	              .append(" (")
	              .append(entry.getValue())
	              .append("회)");

	        if (i != entryList.size() - 1) {
	            result.append(", ");
	        }
	    }

	    return result.toString();
	}
	
	public String extractKeywords(List<Review> reviews) {
	    // 전체 텍스트 결합
	    StringBuilder text = new StringBuilder();
	    for (Review review : reviews) {
	        if (review.getSummary() != null) {
	            text.append(review.getSummary()).append(" ");
	        }
	        if (review.getFeelings() != null) {
	            text.append(review.getFeelings()).append(" ");
	        }
	    }

	    // 명사 기반 키워드 추출 (예: 가장 많이 나온 단어)
	    Map<String, Integer> wordCount = new HashMap<>();
	    String[] words = text.toString().split("[^가-힣a-zA-Z0-9]");

	    for (String word : words) {
	        if (word.length() <= 1) continue; // 1글자 단어 제외

	        if (!wordCount.containsKey(word)) {
	            wordCount.put(word, 1);
	        } else {
	            wordCount.put(word, wordCount.get(word) + 1);
	        }
	    }

	    // 내림차순 정렬
	    List<Map.Entry<String, Integer>> sorted = new ArrayList<>(wordCount.entrySet());
	    for (int i = 0; i < sorted.size() - 1; i++) {
	        for (int j = i + 1; j < sorted.size(); j++) {
	            if (sorted.get(i).getValue() < sorted.get(j).getValue()) {
	                Map.Entry<String, Integer> temp = sorted.get(i);
	                sorted.set(i, sorted.get(j));
	                sorted.set(j, temp);
	            }
	        }
	    }

	    // 상위 키워드 5~10개 뽑기
	    int limit = Math.min(8, sorted.size());
	    StringBuilder keywords = new StringBuilder();
	    for (int i = 0; i < limit; i++) {
	        keywords.append("#").append(sorted.get(i).getKey());
	        if (i != limit - 1) {
	            keywords.append(" ");
	        }
	    }

	    return keywords.toString();
	}
	
	public void generateWeeklySummaryForAllUsers(LocalDate weekStart) {
	    List<Integer> userIds = userService.findAllUserIds(); // 모든 유저 ID 조회

	    for (Integer userId : userIds) {
	        ReviewSummary existing = getWeeklySummaryByStartDate(userId, weekStart);
	        if (existing == null) {
	            generateWeeklyReviewSummary(userId, weekStart);
	        }
	    }
	}
}
