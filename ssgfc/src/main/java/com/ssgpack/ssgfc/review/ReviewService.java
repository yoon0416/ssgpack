package com.ssgpack.ssgfc.review;

import java.time.LocalDate;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository; // db 저장
    private final OpenAIUtil openAIUtil; // ai 요청
    private final ReviewCrawler reviewCrawler; // 크롤링 정보 불러오기

    private static final List<String> SSG_PLAYER_NAMES = Arrays.asList(
        "화이트", "홍대인", "현원회", "한지헌", "한유섬", "한두솔", "하재훈",
        "최현석", "최지훈", "최준우", "최정", "최윤석", "최상민", "최민창", "최민준",
        "천범석", "채현우", "조형우", "조병현", "정현승", "정준재", "정동윤", "전영준",
        "장지훈", "이지영", "이정범", "이율예", "이원준", "이승민", "이로운", "이도우",
        "이건욱", "오태곤", "에레디아", "앤더슨", "안상현", "신헌민", "신지환", "신범수",
        "송영진", "석정우", "서진용", "백승건", "박지환", "박종훈", "박정빈", "박시후",
        "박성한", "박성빈", "박대온", "박기호", "문승원", "류효승", "도재현", "노경은",
        "김현재", "김택형", "김태윤", "김창평", "김찬형", "김수윤", "김성현", "김성민",
        "김민식", "김민", "김규민", "김광현", "김건우", "고명준"
    );

    public ReviewService(ReviewRepository reviewRepository, OpenAIUtil openAIUtil, ReviewCrawler reviewCrawler) {
        this.reviewRepository = reviewRepository;
        this.openAIUtil = openAIUtil;
        this.reviewCrawler = reviewCrawler;
    }
    public void saveSummary(String gameUrl, String summary) throws Exception {
        String dateStr = gameUrl.substring(0, 8);
        LocalDate gameDate = LocalDate.parse(dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8));

        // 기존 리뷰 전체 삭제
        List<Review> oldReviews = reviewRepository.findAllByGameDate(gameDate);
        reviewRepository.deleteAll(oldReviews);

        // 네이버 크롤링 기록 저장
        List<String> records = reviewCrawler.fetchRecords(gameUrl);
        Set<String> uniqueKeys = new HashSet<>();

        for (String record : records) {
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
                review.setGameUrl(gameUrl);
                review.setGameDate(gameDate);
                review.setSummary(null); // 일반 기록은 summary 없음

                reviewRepository.save(review);
            }
        }

        // 마지막에 AI 한줄평 1개 저장
        Review summaryReview = new Review();
        summaryReview.setHow("AI 요약");
        summaryReview.setResult(summary);
        summaryReview.setSummary(summary);
        summaryReview.setGameUrl(gameUrl);
        summaryReview.setGameDate(gameDate);

        reviewRepository.save(summaryReview);
    }
    public void fetchAndSaveReviewByDate(String dateStr) throws Exception {
        LocalDate gameDate = LocalDate.parse(dateStr);

        Optional<Review> optionalReview = reviewRepository.findFirstByGameDate(gameDate);
        if (optionalReview.isEmpty()) {
            throw new IllegalArgumentException("❌ 해당 날짜의 gameUrl 정보를 찾을 수 없습니다.");
        }

        String gameUrl = optionalReview.get().getGameUrl();
        List<Review> oldReviews = reviewRepository.findAllByGameDate(gameDate);
        reviewRepository.deleteAll(oldReviews);

        fetchAndSaveReview(gameUrl);
    }

    public boolean existsByDate(String date) {
        LocalDate localDate = LocalDate.parse(date);
        return reviewRepository.existsByGameDate(localDate);
    }

    public void fetchAndSaveReview(String gameUrl) throws Exception {
        List<String> records = reviewCrawler.fetchRecords(gameUrl);

        if (records.isEmpty()) {
            System.out.println("❌ 가져온 기록이 없습니다.");
            return;
        }

        System.out.println("✅ 가져온 기록 수: " + records.size());

        String dateStr = gameUrl.substring(0, 8);
        LocalDate gameDate = LocalDate.parse(dateStr.substring(0, 4) + "-" + dateStr.substring(4, 6) + "-" + dateStr.substring(6, 8));

        List<Review> reviewList = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>();

        for (String record : records) {
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
                review.setGameUrl(gameUrl);
                review.setGameDate(gameDate);

                reviewList.add(review);
            }
        }

        StringBuilder combined = new StringBuilder();
        List<String> ssgPlayersAppeared = new ArrayList<>();

        for (Review r : reviewList) {
            combined.append("[").append(r.getHow()).append("] ").append(r.getResult()).append("\n");
            for (String player : SSG_PLAYER_NAMES) {
                if (r.getResult().contains(player) && !ssgPlayersAppeared.contains(player)) {
                    ssgPlayersAppeared.add(player);
                }
            }
        }

        String basePrompt = "다음은 SSG 랜더스 야구 경기 주요 기록입니다.\n" +
                "SSG 소속 선수 명단: " + String.join(", ", SSG_PLAYER_NAMES) + "\n" +
                "주요 기록 중 등장하는 선수가 위 명단에 있다면 'SSG 소속 선수'로 간주해 서술하세요.\n" +
                "상대팀 선수는 언급하지 마세요.\n" +
                "50자 이내, 이모지 없이, SSG 팬의 시점에서 간결하고 감성적으로 작성하세요.\n" +
                "주의: 'SSG 팬 입장에서' 같은 문장은 포함하지 마세요.\n" +
                "직접적인 요약 문장만 결과로 반환하세요.\n\n";

        String prompt = basePrompt + combined;

        System.out.println("📝 생성된 프롬프트 내용:\n" + prompt);

        try {
            String summary = openAIUtil.getSummary(prompt);
            System.out.println("🤖 GPT 응답 요약: " + summary);

            for (Review r : reviewList) {
                r.setSummary(summary);
                reviewRepository.save(r);
            }
        } catch (Exception e) {
            System.out.println("❌ GPT 요약 실패: " + e.getMessage());
        }
    }

    public String fetchPreviewSummary(String gameUrl) throws Exception {
        List<String> records = reviewCrawler.fetchRecords(gameUrl);

        if (records.isEmpty()) {
            throw new IllegalArgumentException("❌ 가져온 기록이 없습니다.");
        }

        StringBuilder combined = new StringBuilder();
        for (String record : records) {
            int start = record.indexOf('[');
            int end = record.indexOf(']');
            if (start != -1 && end != -1) {
                String how = record.substring(start + 1, end);
                String result = record.substring(end + 1).trim();
                combined.append("[").append(how).append("] ").append(result).append("\n");
            }
        }

        String basePrompt = "다음은 SSG 랜더스 야구 경기 주요 기록입니다.\n" +
                "SSG 소속 선수 명단: " + String.join(", ", SSG_PLAYER_NAMES) + "\n" +
                "주요 기록 중 등장하는 선수가 위 명단에 있다면 'SSG 소속 선수'로 간주해 서술하세요.\n" +
                "상대팀 선수는 언급하지 마세요.\n" +
                "50자 이내, 이모지 없이, SSG 팬의 시점에서 간결하고 감성적으로 작성하세요.\n" +
                "주의: 'SSG 팬 입장에서' 같은 문장은 포함하지 마세요.\n" +
                "직접적인 요약 문장만 결과로 반환하세요.\n\n";

        String prompt = basePrompt + combined.toString();
        return openAIUtil.getSummary(prompt);
    }

    public String findSummaryByDate(LocalDate date) {
        return reviewRepository.findFirstByGameDateAndHow(date, "AI 요약")
                .map(Review::getSummary)
                .orElse("요약 정보가 없습니다.");
    }
    public List<Review> findByGameDate(LocalDate date) {
    	return reviewRepository.findAllByGameDateAndHowNot(date, "AI 요약");
    }
}
