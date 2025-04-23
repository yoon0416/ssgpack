package com.ssgpack.ssgfc.player;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class PlayerImageUrlInitializer {

    @Autowired
    private PlayerRepository playerRepository;

    @PostConstruct
    public void updateImageUrls() {
        List<Player> players = playerRepository.findAll();
        for (Player player : players) {
            if (player.getImageUrl() == null || player.getImageUrl().isEmpty()) {
                String imageUrl = "/images/players/" + player.getBackNumber() + ".png";
                player.setImageUrl(imageUrl);
                playerRepository.save(player);
                System.out.println("✔ imageUrl 추가됨: " + player.getName() + " → " + imageUrl);
            }
        }
    }
}
