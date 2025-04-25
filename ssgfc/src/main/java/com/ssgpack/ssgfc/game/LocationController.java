package com.ssgpack.ssgfc.game;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    /**
     * 전체 구장 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<Location>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    /**
     * 팀 키로 구장 조회 (예: sk, lg)
     */
    @GetMapping("/team/{teamKey}")
    public ResponseEntity<Location> getByTeamKey(@PathVariable String teamKey) {
        return ResponseEntity.ok(locationService.getByTeamKey(teamKey));
    }

    /**
     * 구장 약칭으로 조회 (예: 문학, 창원)
     */
    @GetMapping("/short/{shortName}")
    public ResponseEntity<Location> getByShortName(@PathVariable String shortName) {
        return ResponseEntity.ok(locationService.getByShortName(shortName));
    }
}