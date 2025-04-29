package com.ssgpack.ssgfc.player;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    public List<Player> getTopPlayersByViewCount(int limit) {
        return playerRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Player::getViewCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
