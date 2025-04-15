package com.ssgpack.ssgfc.player;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class PlayerService {

    @Autowired
    private PlayerRepository playerRepository;

    public List<Player> findAll() {
        return playerRepository.findAll();
    }

    public Player save(Player player) {
        return playerRepository.save(player);
    }

    public Optional<Player> findById(Integer id) {
        return playerRepository.findById(id);
    }

    public void deleteById(Integer id) {
        playerRepository.deleteById(id);
    }

}

/*

*/
