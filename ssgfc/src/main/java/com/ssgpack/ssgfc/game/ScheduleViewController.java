package com.ssgpack.ssgfc.game;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScheduleViewController {

    @GetMapping("/game_schedule")
    public String schedulePage() {
        return "game/game_schedule"; // templates/schedule.html 로 이동
    }
}