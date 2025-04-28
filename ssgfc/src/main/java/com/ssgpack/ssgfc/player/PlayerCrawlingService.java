package com.ssgpack.ssgfc.player;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlayerCrawlingService {

    private final PlayerRepository playerRepository;

    public Player crawlAndSavePlayerBasic(String url, String backNumber) throws IOException {
        Document doc = Jsoup.connect(url).get();

        // 이름 파싱
        Element nameElement = doc.selectFirst("div.t_name");
        String fullName = nameElement.text().trim();
        String koreanName = fullName.split(" ")[0];

        // 기본 정보 파싱
        Elements lis = doc.select("ul li");
        String birthRaw = getText(lis, "생년월일");
        String school = getText(lis, "출신학교");
        String draft = getText(lis, "신인지명");
        String years = getText(lis, "활약연도");
        String teams = getText(lis, "활약팀");

        // pno 추출
        String pno = null;
        if (url.contains("p_no=")) {
            pno = url.substring(url.indexOf("p_no=") + 5);
        }

        // 이미 저장된 경우 return
        if (playerRepository.findTopByPno(pno).isPresent()) {
            System.out.println("✔ 이미 저장된 선수: " + koreanName + " (" + pno + ")");
            return playerRepository.findTopByPno(pno).get();
        }

        // 생년월일 파싱
        LocalDate birthDate = null;
        if (birthRaw != null) {
            birthRaw = birthRaw.replaceAll("[^0-9년월일]", "")
                               .replace("년", "-")
                               .replace("월", "-")
                               .replace("일", "");
            try {
                birthDate = LocalDate.parse(birthRaw);
            } catch (Exception e) {
                System.out.println("⚠ 생일 파싱 실패: " + birthRaw);
            }
        }
        // 등번호에 따라 이미지 URL 생성
        String imageUrl = "/images/players/" + backNumber + ".png";
        
        // Player 객체 생성 및 저장
        Player player = Player.builder()
                .name(koreanName)
                .pno(pno)
                .backNumber(backNumber)
                .birthDate(birthDate)
                .school(school)
                .draftInfo(draft)
                .activeYears(years)
                .teams(teams)
                .imageUrl(imageUrl)
                .build();

        return playerRepository.save(player);
    }
    private String getText(Elements lis, String key) {
        for (Element li : lis) {
            Element label = li.selectFirst("span");
            Element value = li.selectFirst("em");
            if (label != null) {
                String labelText = label.html().replaceAll("<[^>]*>", "").trim();
                if (labelText.equals(key)) {
                    return value != null ? value.text().trim() : "";
                }
            }
        }
        return null;
    }
    public List<Player> findAllPlayers() {
        return playerRepository.findAll();
    }
}
