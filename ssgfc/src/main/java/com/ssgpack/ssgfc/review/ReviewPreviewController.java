package com.ssgpack.ssgfc.review;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ssgpack.ssgfc.player.PlayerRepository;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@RestController
public class ReviewPreviewController {

    private final OpenAIUtil openAIUtil;
    private final ReviewRepository reviewRepository;
    private final PlayerRepository playerRepository;

    public ReviewPreviewController(OpenAIUtil openAIUtil, ReviewRepository reviewRepository, PlayerRepository playerRepository) {
        this.openAIUtil = openAIUtil;
        this.reviewRepository = reviewRepository;
        this.playerRepository = playerRepository;
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
               
                    List<String> playerNames = Arrays.asList(
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
                    
                    StringBuilder combined = new StringBuilder();
                    List<String> ssgPlayersAppeared = new ArrayList<>();
                    
                    for (JsonElement elem : etcRecords) {
                        JsonObject obj = elem.getAsJsonObject();
                        String how = obj.has("how") ? obj.get("how").getAsString() : "";
                        String resultText = obj.has("result") ? obj.get("result").getAsString() : "";
                        combined.append("[").append(how).append("] ").append(resultText).append("\n");

                        for (String player : playerNames) {
                            if (resultText.contains(player) && !ssgPlayersAppeared.contains(player)) {
                                ssgPlayersAppeared.add(player);
                            }
                        }
                    }

                    String playerHighlight = "";
                    if (!ssgPlayersAppeared.isEmpty()) {
                        playerHighlight = "SSG 소속 선수 " + String.join(", ", ssgPlayersAppeared)
                            + "의 활약에도 불구하고 패배했습니다.\n";
                    }
 
                    String basePrompt = "다음은 SSG 랜더스 야구 경기 주요 기록입니다.\n" +
                            "SSG 소속 선수 명단: " + String.join(", ", playerNames) + "\n" +
                            "주요 기록 중 등장하는 선수가 위 명단에 있다면 'SSG 소속 선수'로 간주해 서술하세요.\n" +
                            "상대팀 선수는 언급하지 마세요.\n" +
                            "50자 이내, 이모지 없이, SSG 팬의 시점에서 간결하고 감성적으로 작성하세요.\n" +
                            "**주의: 'SSG 팬 입장에서' 같은 문장은 쓰지 마세요.**\n" +
                            "직접적인 요약 문장만 결과로 반환하세요.\n\n";


                    // ✅ 승리용 prompt
                    String promptWin = basePrompt +
                            "**SSG가 승리한 경기입니다. 승리의 분위기를 살려서 간결하고 신나게 작성하세요.**\n\n"
                            + playerHighlight + combined;

                    // ✅ 패배용 prompt
                    String promptLose = basePrompt +
                            "**SSG가 패배한 경기입니다. 아쉬움을 담되 담백하고 간결하게 작성하세요.**\n\n"
                            + playerHighlight + combined;	

                    // 🔥 여기서 선택해서 사용
                    //String prompt = promptWin;
                    String prompt = promptLose;
                     
                     
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