package com.ssgpack.ssgfc.review;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

                Set<String> uniqueKeys = new HashSet<>();
                List<Review> reviewList = new ArrayList<>();

                String dateStr = game_url.substring(0, 8);
                LocalDate gameDate = LocalDate.parse(dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8));

                for (JsonElement elem : etcRecords) {
                    JsonObject obj = elem.getAsJsonObject();

                    String how = obj.has("how") ? obj.get("how").getAsString() : "";
                    String result = obj.has("result") ? obj.get("result").getAsString() : "";

                    String key = how + "::" + result;
                    if (uniqueKeys.contains(key)) continue; // ✅ 중복이면 무시
                    uniqueKeys.add(key);

                    Review review = new Review();
                    review.setHow(how);
                    review.setResult(result);
                    review.setGameUrl(game_url);
                    review.setGameDate(gameDate);

                    reviewList.add(review);
                }

                // ✅ 항목별로 3개씩 묶어서 줄바꿈 적용 (백엔드 포맷팅)
                Map<String, List<String>> grouped = new LinkedHashMap<>();
                for (Review r : reviewList) {
                    grouped.computeIfAbsent(r.getHow(), k -> new ArrayList<>()).add(r.getResult());
                }

                StringBuilder combined = new StringBuilder();
                for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
                    combined.append("[").append(entry.getKey()).append("] ");
                    List<String> results = entry.getValue();
                    for (int i = 0; i < results.size(); i++) {
                        combined.append(results.get(i));
                        if (i < results.size() - 1) {
                            combined.append(" ");
                        }
                        if ((i + 1) % 3 == 0 && i != results.size() - 1) {
                            combined.append("\n                    ");
                        }
                    }
                    combined.append("\n");
                }

                String prompt = "다음은 SSG 랜더스 야구 경기 주요 기록입니다.\n" +
                        "이모지 없이, SSG 팬의 시점에서 50자 이내로 간결한 한줄 요약을 작성하세요.\n" +
                        "**주의: 'SSG 팬 입장에서' 같은 문장은 포함하지 마세요.**\n" +
                        "직접적인 요약 문장만 결과로 반환하세요.\n\n" + combined;

                System.out.println("📝 생성된 프롬프트 내용:\n" + prompt);

                try {
                    String summary = openAIUtil.getSummary(prompt);
                    System.out.println("🤖 GPT 응답 요약: " + summary);

                    for (Review r : reviewList) {
                        r.setSummary(summary);
                        System.out.println("✅ 실제 저장될 리뷰 확인: " + r.getGameUrl() + " / " + r.getSummary());

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

    public List<Review> findByGameDate(LocalDate date) {
        return reviewRepository.findAllByGameDate(date);
    }
}
