package com.ssgpack.ssgfc.review;

import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAIUtil {

    private final String apiKey;
    private final OkHttpClient client;

    public OpenAIUtil(@Value("${OPENAI_API_KEY}") String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public String getSummary(String prompt) throws Exception {
        // 📌 수정된 프롬프트
        String finalPrompt = "다음은 SSG 랜더스 야구 경기 주요 기록입니다.\n" +
                "이모지 없이, SSG 팬의 시점에서 40~50자 이내로 감정을 담아 짧은 한줄 요약을 작성하세요.\n" +
                "직접적인 요약 문장만 결과로 반환하세요.\n" +
                "단순 정보 나열 대신, 감탄사나 감정을 표현하는 어투를 허용합니다.\n\n" +
                prompt;

        JSONObject json = new JSONObject()
                .put("model", "gpt-4-turbo")
                .put("messages", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", finalPrompt)))
                .put("max_tokens", 150)
                .put("temperature", 0.8); // 🔥 감정 풍부하게 유도 (살짝만 높임)

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        System.out.println("🔥 GPT 요청 보냄: " + finalPrompt);
        System.out.println("🔥 사용된 API Key: " + apiKey);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("❌ GPT 요청 실패: " + response.code());
            }

            String responseBody = response.body().string();
            System.out.println("🔥 GPT 응답: " + responseBody);

            JSONObject res = new JSONObject(responseBody);
            return res.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
                    .trim();
        }
    }
}