package com.kepg.BaseBallLOCK.review.summary.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kepg.BaseBallLOCK.common.ai.OpenAiClient;
import com.kepg.BaseBallLOCK.review.domain.Review;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiSummaryService {
	
    private final OpenAiClient openAiClient;
	
	public String summarizeFeelings(Map<LocalDate, List<String>> dailyFeelings) {
	    StringBuilder promptBuilder = new StringBuilder();
	    promptBuilder.append("다음은 사용자가 야구 리뷰에 남긴 감정 기록입니다.\n")
	                 .append("날짜별 감정 흐름을 분석해서 '날짜 범위: 감정 표현 → 감정 요약' 형식으로 정리해주세요.\n")
	                 .append("반말은 쓰지 마세요. 최대 5줄로 요약해 주세요.\n\n");

	    // 날짜 + 감정 모음 정리
	    for (Map.Entry<LocalDate, List<String>> entry : dailyFeelings.entrySet()) {
	        LocalDate date = entry.getKey();
	        List<String> feelings = entry.getValue();

	        promptBuilder.append(date.toString()).append(": ");
	        for (int i = 0; i < feelings.size(); i++) {
	            promptBuilder.append(feelings.get(i));
	            if (i != feelings.size() - 1) promptBuilder.append(", ");
	        }
	        promptBuilder.append("\n");
	    }

	    String prompt = promptBuilder.toString();

	    // OpenAI API 호출 (직접 구현한 유틸이나 라이브러리 사용)
        return openAiClient.callChatGpt(promptBuilder.toString());
	}
	
    public String generateFullSummary(List<Review> reviews) {
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("다음은 사용자가 일주일간 작성한 리뷰입니다.\n");

        for (Review review : reviews) {
            if (review.getSummary() != null) {
                promptBuilder.append("- ").append(review.getSummary()).append("\n");
            }
        }

        return openAiClient.callChatGpt(promptBuilder.toString());
    }
	
}
