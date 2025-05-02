package com.kepg.BaseBallLOCK.common.ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class GeminiClient {

    @Value("${gemini.api-key}")
    private String apiKey;

    private static final String ENDPOINT =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro-latest:generateContent";
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();

    public String callGemini(String prompt) {
        try {
            Map<String, Object> message = Map.of("parts", List.of(Map.of("text", prompt)));
            Map<String, Object> request = Map.of("contents", List.of(message));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(gson.toJson(request), headers);

            String url = ENDPOINT + "?key=" + apiKey;
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                JsonObject root = JsonParser.parseString(response.getBody()).getAsJsonObject();
                JsonArray candidates = root.getAsJsonArray("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                    return content.getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString().trim();
                }
            }

        } catch (Exception e) {
            log.error("Gemini 호출 실패", e);
        }

        return "Gemini 응답을 불러오지 못했습니다.";
    }
}