package com.ssgpack.ssgfc.review;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OpenAIUtil openAIUtil;

    public ReviewService(ReviewRepository reviewRepository, OpenAIUtil openAIUtil) {
        this.reviewRepository = reviewRepository;
        this.openAIUtil = openAIUtil;
    }

    public boolean existsByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return reviewRepository.existsByGameDate(localDate);
    }

    public void fetchAndSaveReview(String game_url) throws IOException {
        String url = "https://api-gw.sports.naver.com/schedule/games/" + game_url + "/record";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "https://m.sports.naver.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String json = response.body().string();
                System.out.println(json);

                JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                JsonArray etcRecords = root
                        .getAsJsonObject("result")
                        .getAsJsonObject("recordData")
                        .getAsJsonArray("etcRecords");

                System.out.println("✅ 크롤링 데이터 수: " + etcRecords.size());

                List<Review> reviewList = new ArrayList<>();

                String dateStr = game_url.substring(0, 8);
                LocalDate gameDate = LocalDate.parse(dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8));

                for (JsonElement elem : etcRecords) {
                    JsonObject obj = elem.getAsJsonObject();

                    String how = obj.has("how") ? obj.get("how").getAsString() : "";
                    String result = obj.has("result") ? obj.get("result").getAsString() : "";

                    Review review = new Review();
                    review.setHow(how);
                    review.setResult(result);
                    review.setGameUrl(game_url);
                    review.setGameDate(gameDate);

                    reviewList.add(review);
                }

                // GPT 요약용 프롬프트 생성
                StringBuilder combined = new StringBuilder();
                for (Review r : reviewList) {
                    combined.append("[").append(r.getHow()).append("] ").append(r.getResult()).append("\n");
                }

                String prompt = "다음은 야구 경기 기록입니다. 이모지 쓰지않고 SSG팬 입장에서의 경기 요약 한줄평\n" + combined;
                System.out.println("📝 생성된 프롬프트 내용:\n" + prompt);

                try {
                    String summary = openAIUtil.getSummary(prompt);
                    System.out.println("🤖 GPT 응답 요약: " + summary);

                    for (Review r : reviewList) {
                        r.setSummary(summary);
                        System.out.println("✅ 실제 저장될 리뷰 확인: " + r.getGameUrl() + " / " + r.getSummary());

                        // ✅ 중복 저장 방지 후 저장
                        if (!reviewRepository.existsByGameUrlAndHowAndResult(r.getGameUrl(), r.getHow(), r.getResult())) {
                            reviewRepository.save(r);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("❌ GPT 요약 실패: " + e.getMessage());
                }
            }
        }
    }

    public String findSummaryByDate(LocalDate date) {
        return reviewRepository.findFirstByGameDate(date)
                .map(Review::getSummary)
                .orElse("요약 정보가 없습니다.");
    }
}
