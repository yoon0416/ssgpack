package com.ssgpack.ssgfc.review;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository; //db저장
    private final OpenAIUtil openAIUtil; //ai 요청
    private final ReviewCrawler reviewCrawler; // 크롤링정보 불러오기

    public ReviewService(ReviewRepository reviewRepository, OpenAIUtil openAIUtil, ReviewCrawler reviewCrawler) {
        this.reviewRepository = reviewRepository;
        this.openAIUtil = openAIUtil;
        this.reviewCrawler = reviewCrawler;
    }

    public boolean existsByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return reviewRepository.existsByGameDate(localDate);
    } // 해당날짜 DB기록 여부 확인

    public void fetchAndSaveReview(String game_url) throws Exception {
        // ✅ 크롤링한 기록 가져오기
        List<String> records = reviewCrawler.fetchRecords(game_url);

        if (records.isEmpty()) {
            System.out.println("❌ 가져온 기록이 없습니다.");
            return;
        }

        System.out.println("✅ 가져온 기록 수: " + records.size());

        String dateStr = game_url.substring(0, 8); //URL 주소중 8글자
        LocalDate gameDate = LocalDate.parse(dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8));

        List<Review> reviewList = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>(); // 크롤링한 정보중 중복 기록 제거

        for (String record : records) {
            // [how] result 형식 분리
            int start = record.indexOf('[');
            int end = record.indexOf(']');
            if (start != -1 && end != -1) {
                String how = record.substring(start + 1, end);
                String result = record.substring(end + 1).trim();

                String key = how + "::" + result;
                if (uniqueKeys.contains(key)) continue;
                uniqueKeys.add(key);

                Review review = new Review();
                review.setHow(how);
                review.setResult(result);
                review.setGameUrl(game_url);
                review.setGameDate(gameDate);

                reviewList.add(review);
            }
        }

        // ✅ prompt 생성(요청형식)
        StringBuilder combined = new StringBuilder();
        for (Review r : reviewList) {
            combined.append("[").append(r.getHow()).append("] ").append(r.getResult()).append("\n");
        }

        String prompt = "다음은 SSG 랜더스 야구 경기 주요 기록입니다.\n" +
                "이모지 없이, SSG 팬의 시점에서 50자 이내로 간결한 한줄 요약을 작성하세요.\n" +
                "**주의: 'SSG 팬 입장에서' 같은 문장은 포함하지 마세요.**\n" +
                "직접적인 요약 문장만 결과로 반환하세요.\n\n" + combined;

        System.out.println("📝 생성된 프롬프트 내용:\n" + prompt);

        // ✅ AI 요약 요청결과 출력
        try {
            String summary = openAIUtil.getSummary(prompt);
            System.out.println("🤖 GPT 응답 요약: " + summary);

            
            
            for (Review r : reviewList) {
                r.setSummary(summary);

                if (!reviewRepository.existsByGameUrlAndHowAndResult(r.getGameUrl(), r.getHow(), r.getResult())) {
                    reviewRepository.save(r);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ GPT 요약 실패: " + e.getMessage());
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
