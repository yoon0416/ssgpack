package com.ssgpack.ssgfc.player;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PlayerCrawlService {

	public Player crawlPlayer(String playerId) throws IOException {
        String url = "https://statiz.sporki.com/player/?m=playerinfo&p_no=" + playerId;
        Document doc = Jsoup.connect(url).userAgent("Mozilla").get();

        // 이름
        String name = doc.selectFirst("div.subhead > h1").text();

        // 테이블 정보
        Elements rows = doc.select("div.playerinfo_box table tr");

        String birth = null, position = null, debut = null;
        for (Element row : rows) {
            Elements tds = row.select("td");
            if (tds.size() == 2) {
                String key = tds.get(0).text().trim();
                String value = tds.get(1).text().trim();

                switch (key) {
                    case "출생":
                        birth = value;
                        break;
                    case "포지션":
                        position = value;
                        break;
                    case "데뷔":
                        debut = value;
                        break;
                }
            }
        }

        return new Player(null, name, birth, position, debut);
    }
}
