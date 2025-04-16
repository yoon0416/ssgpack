package com.ssgpack.ssgfc.player;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

@Service
public class PlayerCrawlService {

    public List<Player> crawlSSGPlayers() {
        List<Player> players = new ArrayList<>();

        System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // 중요! 기존 headless는 오류 발생
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://www.koreabaseball.com/Record/Player/HitterBasic/Basic1.aspx?teamCode=SSG");

            List<WebElement> rows = driver.findElements(By.cssSelector("table.tData tbody tr"));

            for (WebElement row : rows) {
                List<WebElement> cols = row.findElements(By.tagName("td"));
                if (cols.size() > 3) {
                    String name = cols.get(1).getText().trim();
                    String avgStr = cols.get(3).getText().trim();

                    Player player = new Player();
                    player.setName(name);
                    player.setPosition("SSG");
                    try {
                        player.setAvg(Double.parseDouble(avgStr));
                    } catch (NumberFormatException e) {
                        player.setAvg(0.0);
                    }

                    System.out.println("✅ 이름: " + player.getName() + ", 타율: " + player.getAvg());
                    players.add(player);
                }
            }

        } catch (Exception e) {
            System.out.println("❌ 크롤링 실패: " + e.getMessage());
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return players;
    }
}