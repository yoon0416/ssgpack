package com.ssgpack.ssgfc.weather;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssgpack.ssgfc.game.GameSchedule;
import com.ssgpack.ssgfc.game.GameScheduleRepository;
import com.ssgpack.ssgfc.game.Location;
import com.ssgpack.ssgfc.game.LocationRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameWeatherController {

    private final GameScheduleRepository gameScheduleRepository;
    private final WeatherService weatherService;
    private final LocationRepository locationRepository;

    @GetMapping("/today-games")
    public ResponseEntity<List<Map<String, Object>>> todayGames() {
        LocalDate today = LocalDate.now();

        // 🔍 오늘 SSG 경기만 필터링
        List<GameSchedule> ssgGames = gameScheduleRepository.findByGameDate(today).stream()
                .filter(g -> "SSG".equalsIgnoreCase(g.getTeam1()) || "SSG".equalsIgnoreCase(g.getTeam2()))
                .collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();

        for (GameSchedule game : ssgGames) {
            String stadium = game.getLocation();

            // 📌 location이 null이면 SSG 팀키(sk)로 강제 매칭
            if (stadium == null) {
                Location loc = locationRepository.findByTeamKey("sk");
                if (loc == null) {
                    System.out.println("🚨 teamKey 'sk' 매칭 실패");
                    continue;
                }
                stadium = loc.getStadiumName();
            }

            Location loc = locationRepository.findByStadiumName(stadium);
            if (loc == null) {
                System.out.println("🚨 location 테이블에 없는 구장: " + stadium);
                continue;
            }

            String opponent = game.getTeam1().equalsIgnoreCase("SSG") ? game.getTeam2() : game.getTeam1();

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("stadium", loc.getStadiumName());
            map.put("team", opponent);
            map.put("top", loc.getTopPercent());
            map.put("left", loc.getLeftPercent());
            map.put("weather", weatherService.getStadiumWeather(
                    loc.getTeamKey().toUpperCase(), loc.getNx(), loc.getNy()
            ));

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }
    @GetMapping("/next-game")
    public ResponseEntity<Map<String, String>> nextGame(@RequestParam String team) {
        LocalDate today = LocalDate.now();

        // SSG 팀 경기를 teamKey로 찾자
        GameSchedule nextGame = gameScheduleRepository.findByGameDateAfter(today).stream()
                .filter(g -> "SSG".equalsIgnoreCase(g.getTeam1()) || "SSG".equalsIgnoreCase(g.getTeam2()))
                .sorted((g1, g2) -> g1.getGameDate().compareTo(g2.getGameDate()))
                .findFirst()
                .orElse(null);

        Map<String, String> result = new HashMap<>();

        if (nextGame == null) {
            result.put("message", "향후 일정이 없습니다");
        } else {
            result.put("date", nextGame.getGameDate().toString());
            result.put("opponent", nextGame.getTeam1().equals("SSG") ? nextGame.getTeam2() : nextGame.getTeam1());
        }

        return ResponseEntity.ok(result);
    }
}
