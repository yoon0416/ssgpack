package com.ssgpack.ssgfc.review;

import com.google.gson.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReviewCrawler {

    private final OkHttpClient client = new OkHttpClient();

    public List<String> fetchRecords(String gameUrl) throws Exception {
        String url = "https://api-gw.sports.naver.com/schedule/games/" + gameUrl + "/record";

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://m.sports.naver.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();

                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                JsonObject result = root.getAsJsonObject("result");
                if (result == null || !result.has("recordData")) {
                    throw new Exception("❌ recordData 없음");
                }

                JsonObject recordData = result.getAsJsonObject("recordData");
                if (recordData == null || !recordData.has("etcRecords")) {
                    throw new Exception("❌ etcRecords 없음");
                }

                JsonArray etcRecords = recordData.getAsJsonArray("etcRecords");
                List<String> records = new ArrayList<>();

                for (JsonElement elem : etcRecords) {
                    JsonObject obj = elem.getAsJsonObject();
                    String how = obj.has("how") ? obj.get("how").getAsString() : "";
                    String resultText = obj.has("result") ? obj.get("result").getAsString() : "";

                    if (!how.isEmpty() && !resultText.isEmpty()) {
                        records.add("[" + how + "] " + resultText);
                    }
                }

                return records;
            } else {
                throw new Exception("❌ 네이버 API 요청 실패: " + response.code());
            }
        }
    }
}