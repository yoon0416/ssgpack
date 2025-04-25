package com.ssgpack.ssgfc.weather;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssgpack.ssgfc.game.GameSchedule;
import com.ssgpack.ssgfc.game.GameScheduleRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GameWeatherController {

    private final GameScheduleRepository gameScheduleRepository;
    private final WeatherService weatherService;
    
    private static final Map<String, String> stadiumToKey = Map.of(
    	    "인천SSG랜더스필드", "sk",
    	    "고척돔", "kiwoom",
    	    "잠실야구장", "doosan",
    	    "잠실", "doosan",
    	    "대구라이온즈파크", "samsung",
    	    "사직야구장", "lotte",
    	    "창원NC파크", "nc",
    	    "수원KT위즈파크", "kt",
    	    "대전한화생명이글스파크", "hanwha",
    	    "광주챔피언스필드", "kia"
    	);
    
    private static final Map<String, String> teamNameKorToKey = Map.of(
    	    "SSG", "sk", "키움", "kiwoom", "두산", "doosan", "LG", "lg", "삼성", "samsung",
    	    "한화", "hanwha", "KT", "kt", "KIA", "kia", "롯데", "lotte", "NC", "nc"
    	);
    
    private static final Map<String, String> teamStadiumMap = Map.of(
        "sk", "인천SSG랜더스필드", "doosan", "잠실야구장", "kiwoom", "고척돔",
        "samsung", "대구라이온즈파크", "lotte", "사직야구장", "nc", "창원NC파크",
        "kt", "수원KT위즈파크", "hanwha", "대전한화생명이글스파크",
        "kia", "광주챔피언스필드", "lg", "잠실야구장"
    );

    private static final Map<String, String> topMap = Map.of(
        "sk", "28%", "doosan", "32%", "kiwoom", "33%", "samsung", "65%",
        "lotte", "75%", "nc", "72%", "kt", "40%", "hanwha", "53%", "kia", "72%", "lg", "34%"
    );

    private static final Map<String, String> leftMap = Map.of(
        "sk", "38%", "doosan", "50%", "kiwoom", "47%", "samsung", "62%",
        "lotte", "70%", "nc", "65%", "kt", "50%", "hanwha", "52%", "kia", "48%", "lg", "52%"
    );

    private static final Map<String, int[]> nxnyMap = Map.of(
        "sk", new int[]{54,124}, "doosan", new int[]{62,126}, "kiwoom", new int[]{58,125},
        "samsung", new int[]{89,90}, "lotte", new int[]{98,76}, "nc", new int[]{91,77},
        "kt", new int[]{61,121}, "hanwha", new int[]{67,100}, "kia", new int[]{58,74}, "lg", new int[]{62,126}
    );

    @GetMapping("/today-games")
    public ResponseEntity<List<Map<String, Object>>> todayGames() {
        LocalDate today = LocalDate.now();

        // ✅ SSG 경기가 있는 경우만 필터링
        List<GameSchedule> games = gameScheduleRepository.findByGameDate(today).stream()
            .filter(g -> 
                "SSG".equalsIgnoreCase(g.getTeam1()) || "SSG".equalsIgnoreCase(g.getTeam2())
            )
            .collect(Collectors.toList());

        for(GameSchedule g : games){
            System.out.println("📌 SSG 경기의 location값: " + g.getLocation());
        }

        Set<String> stadiums = games.stream()
            .map(GameSchedule::getLocation)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        List<Map<String, Object>> result = new ArrayList<>();

        for (String stadium : stadiums) {
            String key = stadiumToKey.get(stadium);
            if (key == null || !nxnyMap.containsKey(key)) {
                System.out.println("🚨 매핑 안 된 경기장 있음: " + stadium + ", key: " + key);
                continue;
            }

            int[] xy = nxnyMap.get(key);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("stadium", stadium);
            map.put("team", key);
            map.put("top", topMap.get(key));
            map.put("left", leftMap.get(key));
            map.put("weather", weatherService.getStadiumWeather(key.toUpperCase(), xy[0], xy[1]));

            result.add(map);
        }

        return ResponseEntity.ok(result);
    }
}
