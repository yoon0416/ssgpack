package com.ssgpack.ssgfc.weather;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;
    
    @GetMapping("/{stadium}")
    public ResponseEntity<Map<String, Object>> getWeather(@PathVariable String stadium) {
    	//구장명 별 격자 좌표
    	Map<String, int[]> gridMap = Map.of(
    			"sk", new int[] {54,124},
    			"doosan", new int[] {62,126},
    			"kiwoom", new int[]{58, 125},
                "samsung", new int[]{89, 90},
                "lotte", new int[]{98, 76},
                "nc", new int[]{91, 77},
                "kt", new int[]{61, 121},
                "hanwha", new int[]{67, 100},
                "kia", new int[]{58, 74},
                "lg", new int[]{62, 126}
    			);
    	String key = stadium.toLowerCase();
    	if(!gridMap.containsKey(key)) {
    		return ResponseEntity.status(404).body(Map.of("error" , "stadium not found"));
    	}
    	int[] xy = gridMap.get(key);
    	Map<String, Object> weatherData = weatherService.getStadiumWeather(stadium.toUpperCase(),xy[0], xy[1]);
    			return ResponseEntity.ok(weatherData);
    }
}
