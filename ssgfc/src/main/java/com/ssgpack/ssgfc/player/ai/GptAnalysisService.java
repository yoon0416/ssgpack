package com.ssgpack.ssgfc.player.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GptAnalysisService {

    @Value("${OPENAI_API_KEY}")
    private String openaiApiKey;

    private final String GPT_URL = "https://api.openai.com/v1/chat/completions";

    public String analyze(String playerJson) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Object> systemMsg = Map.of("role", "system", "content", "너는 한국 야구 해설자야. 스탯을 기반으로 선수 분석을 해줘.");
        Map<String, Object> userMsg = Map.of("role", "user", "content", "이 선수 기록을 비교 분석해줘:\n" + playerJson);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "gpt-3.5-turbo");
        body.put("messages", List.of(systemMsg, userMsg));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(body), headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(GPT_URL, entity, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}
