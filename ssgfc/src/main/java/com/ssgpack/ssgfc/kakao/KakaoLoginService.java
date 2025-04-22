package com.ssgpack.ssgfc.kakao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class KakaoLoginService {

    @Value("${kakao_redirect_url}")
    private String kakaoRedirectUrl;

    @Value("${kakao_api}")
    private String kakaoApi;

    // STEP 1: 로그인 URL 생성
    public String step1() {
        return "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + kakaoApi
                + "&redirect_uri=" + kakaoRedirectUrl;
    }

    // STEP 2: 인가코드를 액세스토큰으로 바꿈
    public List<String> step2(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token"
                + "?grant_type=authorization_code"
                + "&client_id=" + kakaoApi
                + "&redirect_uri=" + kakaoRedirectUrl
                + "&code=" + code;

        String tokenJson = requestTo(tokenUrl, "POST", "application/x-www-form-urlencoded;charset=utf-8", null);
        JsonObject tokenObj = JsonParser.parseString(tokenJson).getAsJsonObject();
        String accessToken = tokenObj.get("access_token").getAsString();

        return step3(accessToken);
    }

    // STEP 3: 액세스토큰으로 사용자 정보 요청
    public List<String> step3(String token) {
        String userUrl = "https://kapi.kakao.com/v2/user/me";

        String response = requestTo(
                userUrl,
                "POST",
                "application/x-www-form-urlencoded;charset=utf-8",
                "Bearer " + token
        );

        JsonObject obj = JsonParser.parseString(response).getAsJsonObject();
        JsonObject properties = obj.getAsJsonObject("properties");
        JsonObject account = obj.getAsJsonObject("kakao_account");

        List<String> userInfo = new ArrayList<>();
        userInfo.add(properties.get("nickname").getAsString());          // 0: 닉네임
        userInfo.add(properties.get("profile_image").getAsString());     // 1: 프로필 이미지

        // 이메일 권한이 없으면 null-safe 처리
        if (account.has("email") && !account.get("email").isJsonNull()) {
            userInfo.add(account.get("email").getAsString()); // 2: 이메일
        } else {
            String randomEmail = "kakaouser" + UUID.randomUUID().toString().substring(0, 8) + "@kakao.com";
            userInfo.add(randomEmail); // 2: 임시 이메일
        }

        userInfo.add(obj.get("id").getAsString());                       // 3: 카카오 ID

        return userInfo;
    }

    // 공통 요청 처리 메서드
    private String requestTo(String urlStr, String method, String contentType, String authorization) {
        StringBuffer result = new StringBuffer();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", contentType);
            if (authorization != null) {
                conn.setRequestProperty("Authorization", authorization);
            }

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getResponseCode() == 200
                            ? conn.getInputStream()
                            : conn.getErrorStream()
                    )
            );

            String line;
            while ((line = br.readLine()) != null) {
                result.append(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result.toString();
    }
}
