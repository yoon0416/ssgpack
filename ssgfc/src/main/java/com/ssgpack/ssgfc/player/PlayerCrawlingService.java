package com.ssgpack.ssgfc.player;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PlayerCrawlingService {

    private final PlayerRepository playerRepository;

    private final String baseUrl = "https://statiz.sporki.com";

    @Transactional
    public int crawlAndSaveSSGPlayers() {
        int saveCount = 0;
        try {
            // 1. 등번호 페이지 접근
            String backnumberUrl = baseUrl + "/team/?m=seasonBacknumber&t_code=9002&year=2025";
            Document doc = Jsoup.connect(backnumberUrl)
            	    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            	    .get();

            // 2. 선수 상세 링크 추출
            Elements playerLinks = doc.select(".back_number_area .item.away a");

            for (Element link : playerLinks) {
                String href = link.attr("href");
                String fullUrl = baseUrl + href;

                try {
                    // 3. 상세 페이지 요청
                	Document detailDoc = Jsoup.connect(fullUrl)
                		    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                		    .get();

                    // 4. 파싱 및 저장
                    Player player = parsePlayer(detailDoc);
                    if (player != null) {
                        playerRepository.save(player);
                        saveCount++;
                    }

                    // ✅ 차단 방지용 요청 간 1초 대기
                    Thread.sleep(1000);

                } catch (Exception e) {
                    System.out.println("❌ 선수 처리 실패: " + fullUrl);
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return saveCount;
    }

    private Player parsePlayer(Document doc) {
    	try {
    	    // 이름은 <title> 태그에서 추출 (예: "박성한 - STATIZ")
    	    String title = doc.title();  // <title>박성한 - STATIZ</title>
    	    String name = title.split(" - ")[0].trim();
    	    System.out.println("✅ 이름: " + name);

    	    // info 박스 추출
    	    Element infoBox = doc.selectFirst(".player_info_area .info");
    	    if (infoBox == null) {
    	        System.out.println("❌ info 박스 못 찾음");
    	        return null;
    	    }

    	    String text = infoBox.text();
    	    System.out.println("✅ info 텍스트: " + text);

            // 등번호
            Pattern backNumberPattern = Pattern.compile("등번호\\s*(\\d+)");
            Matcher matcher = backNumberPattern.matcher(text);
            int backNumber = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;

            // 포지션
            String position = getTextAfterLabel(text, "포지션");

            // 생년월일
            String birth = getTextAfterLabel(text, "생년월일");
            LocalDate birthDate = LocalDate.parse(birth.replace(".", "-"));

            // 신체
            String body = getTextAfterLabel(text, "신체")
                    .replace("cm", "")
                    .replace("kg", "")
                    .replace(" ", "");
            String[] parts = body.split("/");
            int height = Integer.parseInt(parts[0]);
            int weight = Integer.parseInt(parts[1]);

            // 데뷔연도
            String debutStr = getTextAfterLabel(text, "데뷔");
            int debutYear = Integer.parseInt(debutStr.substring(0, 4));

            // 사진
            Element photoEl = doc.selectFirst(".player_img img");
            String photoUrl = (photoEl != null) ? photoEl.attr("src") : "";

            return Player.builder()
                    .name(name)
                    .backNumber(backNumber)
                    .position(position)
                    .birthDate(birthDate)
                    .height(height)
                    .weight(weight)
                    .debutYear(debutYear)
                    .photoUrl(photoUrl)
                    .build();

        } catch (Exception e) {
            System.out.println("선수 파싱 실패: " + e.getMessage());
            return null;
        }
    }

    private String getTextAfterLabel(String text, String label) {
        int idx = text.indexOf(label);
        if (idx == -1) return "";
        String sub = text.substring(idx + label.length()).trim();
        return sub.split(" ")[0].trim();
    }
}
