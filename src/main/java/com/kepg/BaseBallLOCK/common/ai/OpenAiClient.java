package com.kepg.BaseBallLOCK.common.ai;

import org.springframework.stereotype.Component;

@Component
public class OpenAiClient {

    // 실제 OpenAI 호출 대신, 더미 텍스트를 반환
    public String callChatGpt(String prompt) {
        if (prompt.contains("감정")) {
            return "🧠 감정 요약: 분노 → 희망 → 냉소. 감정의 롤러코스터였습니다.";
        } else {
            return "💬 요약 멘트: 승리는 단 하루, 나머지는 좌절의 연속이었습니다.";
        }
    }
}