package com.ssgpack.ssgfc.game;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /**
     * ✅ 전체 구장 목록 조회 (teamKey 포함)
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllLocations() {
        List<Location> locations = locationService.getAllLocations();

        List<Map<String, Object>> result = locations.stream().map(loc -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("stadiumName", loc.getStadiumName());
            map.put("topPercent", loc.getTopPercent());
            map.put("leftPercent", loc.getLeftPercent());
            map.put("teamKey", loc.getTeamKey());
            map.put("shortName", loc.getShortName());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * ✅ 팀 키로 구장 조회 (예: sk, lg)
     */
    @GetMapping("/team/{teamKey}")
    public ResponseEntity<Location> getByTeamKey(@PathVariable String teamKey) {
        return ResponseEntity.ok(locationService.getByTeamKey(teamKey));
    }

    /**
     * ✅ 구장 약칭으로 조회 (예: 문학, 창원)
     */
    @GetMapping("/short/{shortName}")
    public ResponseEntity<Location> getByShortName(@PathVariable String shortName) {
        return ResponseEntity.ok(locationService.getByShortName(shortName));
    }
}
