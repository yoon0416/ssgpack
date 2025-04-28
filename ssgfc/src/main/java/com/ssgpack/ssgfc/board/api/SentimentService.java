package com.ssgpack.ssgfc.board.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SentimentService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ JSON 파싱용

    @Value("${OPENAI_API_KEY}")
    private String apiKey;  // ✅ ChatGPT API Key

    // ✅ ChatGPT 3.5 API URL
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * ✅ ChatGPT API 호출하여 감정 분석
     * 한글 텍스트의 감정을 "긍정", "부정", "중립"으로 분석
     */
    public String analyzeSentiment(String content) {
        String result = "NEUTRAL"; // 기본값 (감정 분석 실패 시 중립)

        try {
            // ✅ API 요청 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey); // ✅ API Key 설정

            // ✅ 요청 본문 설정 (ChatGPT 3.5 모델 사용)
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-3.5-turbo"); // ✅ 사용 모델
            body.put("messages", List.of(
            		Map.of("role", "system", "content", 
            			    "Analyze the following text and ONLY answer with one word: Positive, Negative, or Neutral. No explanation, just one of the three words."),
                    Map.of("role", "user", "content", content)
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // ✅ API 호출
            System.out.println("Request Body: " + body); // ✅ 요청 본문 출력
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);

            // ✅ 응답 처리
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                System.out.println("API Response: " + response.getBody()); // ✅ 응답 본문 출력
                // ✅ 감정 분석 결과 처리
                String label = root.path("choices").get(0).path("message").path("content").asText();
                System.out.println("Sentiment Label: " + label); // ✅ 감정 분석 결과 출력
                result = mapSentiment(label);  // ✅ 감정 분석 결과 매핑
            }

        } catch (Exception e) {
            System.out.println("감정 분석 실패: " + e.getMessage());
        }

        // ✅ 최종 분석 결과 출력
        System.out.println("최종 분석 결과 = " + result);
        return result;
    }

    /**
     * ✅ ChatGPT에서 받은 감정 결과를 "긍정", "부정", "중립"으로 매핑
     * - "긍정", "부정", "중립"만 받아오기 위해 명확한 요구를 했음.
     */
    private String mapSentiment(String label) {
        // 3단계로 분석하도록 명시적으로 요청했으므로 3단계 매핑만 사용
        switch (label) {
            case "Negative":
                return "NEGATIVE"; // ✅ 부정
            case "Positive":
                return "POSITIVE"; // ✅ 긍정
            case "Neutral":
                return "NEUTRAL"; // ✅ 중립
            default:
                return "NEUTRAL"; // 예외 처리 (기타 상황은 중립)
        }
    }
}
