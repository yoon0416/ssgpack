package com.ssgpack.ssgfc.naver;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NaverLoginService {

    @Value("${naver.client.id}")
    private String clientId;

    @Value("${naver.client.secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();

    // Step 1: 네이버 로그인 URL 생성
    public String step1() {
        return "https://nid.naver.com/oauth2.0/authorize?" +
                "client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&state=naver";
    }

    // Step 2: code로 토큰 받고 사용자 정보 조회
    public Map<String, String> step2(String code) {
        // 1. 토큰 요청
        String tokenUrl = "https://nid.naver.com/oauth2.0/token?" +
                "grant_type=authorization_code" +
                "&client_id=" + clientId +
                "&client_secret=" + clientSecret +
                "&code=" + code +
                "&state=naver";

        ResponseEntity<String> tokenResponse = restTemplate.getForEntity(tokenUrl, String.class);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> tokenMap = objectMapper.readValue(tokenResponse.getBody(), Map.class);

            String accessToken = (String) tokenMap.get("access_token");

            // 2. 사용자 정보 요청
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            HttpEntity<?> request = new HttpEntity<>(headers);

            ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                    "https://openapi.naver.com/v1/nid/me",
                    HttpMethod.GET,
                    request,
                    String.class
            );

            Map<String, Object> userInfoMap = objectMapper.readValue(userInfoResponse.getBody(), Map.class);
            Map<String, Object> response = (Map<String, Object>) userInfoMap.get("response");

            Map<String, String> infos = new HashMap<>();
            infos.put("email", (String) response.get("email"));
            infos.put("name", (String) response.get("name"));
            infos.put("id", (String) response.get("id"));  // 네이버 고유 id

            return infos;
        } catch (Exception e) {
            throw new RuntimeException("네이버 사용자 정보 파싱 실패", e);
        }
    }
}
