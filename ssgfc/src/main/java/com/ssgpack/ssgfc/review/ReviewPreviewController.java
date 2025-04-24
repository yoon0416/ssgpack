package com.ssgpack.ssgfc.review;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RestController
public class ReviewPreviewController {

    private final OpenAIUtil openAIUtil;
    private final ReviewRepository reviewRepository;

    public ReviewPreviewController(OpenAIUtil openAIUtil, ReviewRepository reviewRepository) {
        this.openAIUtil = openAIUtil;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/api/review-preview")
    public String previewSummary(@RequestParam String date) {
        try {
            LocalDate localDate = LocalDate.parse(date);
            Optional<Review> optionalReview = reviewRepository.findFirstByGameDate(localDate);
            if (optionalReview.isEmpty()) {
                return "❌ 미리보기 실패: 해당 날짜의 리뷰가 없습니다.";
            }

            String gameUrl = optionalReview.get().getGameUrl();
            String url = "https://api-gw.sports.naver.com/schedule/games/" + gameUrl + "/record";

            OkHttpClient client = new OkHttpClient();
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
                    JsonObject recordData = result.getAsJsonObject("recordData");

                    if (recordData == null || !recordData.has("etcRecords")) {
                        return "❌ 미리보기 실패: 기록 데이터 없음";
                    }

                    JsonArray etcRecords = recordData.getAsJsonArray("etcRecords");

                    StringBuilder combined = new StringBuilder();
                    for (JsonElement elem : etcRecords) {
                        JsonObject obj = elem.getAsJsonObject();
                        String how = obj.has("how") ? obj.get("how").getAsString() : "";
                        String resultText = obj.has("result") ? obj.get("result").getAsString() : "";
                        combined.append("[").append(how).append("] ").append(resultText).append("\n");
                    }

                    String prompt = "다음은 SSG 랜더스 야구 경기 주요 기록입니다.\n" +
                            "이모지 없이, SSG 팬의 시점에서 50자 이내로 간결한 한줄 요약을 작성하세요.\n" +
                            "**주의: 'SSG 팬 입장에서' 같은 문장은 포함하지 마세요.**\n" +
                            "직접적인 요약 문장만 결과로 반환하세요.\n\n" + combined;

                    System.out.println("🔥 [PREVIEW] 프롬프트:\n" + prompt);
                    return openAIUtil.getSummary(prompt);
                } else {
                    return "❌ 크롤링 실패: API 응답 오류";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "❌ 미리보기 실패: " + e.getMessage();
        }
    }
}