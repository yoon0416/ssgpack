package com.ssgpack.ssgfc.player;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class PlayerController{
	@Autowired
	private PlayerService playerService;
	
	@GetMapping
	private List<Player> getAllPlayers(){
		return playerService.findAll();
	}
    @PostMapping
    public Player createPlayer(@RequestBody Player player) {
        return playerService.save(player);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayerById(@PathVariable Integer id) {
        return playerService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void deletePlayer(@PathVariable Integer id) {
        playerService.deleteById(id);
    }
	
	
}



/*@Controller
public class PlayerController {



}*/
