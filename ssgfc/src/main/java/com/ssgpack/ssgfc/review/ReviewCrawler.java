package com.ssgpack.ssgfc.review;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ReviewCrawler {
    public static void main(String[] args) {
        String url = "https://api-gw.sports.naver.com/schedule/games/20250420LGSK02025/record";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://m.sports.naver.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();

                // JSON 파싱
                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                JsonObject result = root.getAsJsonObject("result");
                JsonObject recordData = result.getAsJsonObject("recordData");
                JsonArray etcRecords = recordData.getAsJsonArray("etcRecords");

                System.out.println("=== ⚾ 경기 기록 (ReviewRecords) ===");
                for (JsonElement recordElem : etcRecords) {
                    JsonObject record = recordElem.getAsJsonObject();
                    String resultText = record.get("result").getAsString();
                    String how = record.get("how").getAsString();
                    System.out.println("- [" + how + "] " + resultText);
                }
            } else {
                System.out.println("❌ API 응답 실패: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}