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
        JSONObject json = new JSONObject()
                .put("model", "gpt-3.5-turbo")
                .put("messages", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", prompt)))
                .put("max_tokens", 100)
                .put("temperature", 0.7);

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

        System.out.println("🔥 GPT 요청 보냄: " + prompt);
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
