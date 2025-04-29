package com.ssgpack.ssgfc.game;

import java.time.LocalDate;
import java.util.List;

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
    private final LocationCrawler locationCrawler;
    private final GameScheduleRepository gameScheduleRepository;

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
    
    @GetMapping("/update-game-schedule-locations")
    public ResponseEntity<String> updateGameScheduleLocations() {
        locationCrawler.crawlAndUpdateLocations();
        return ResponseEntity.ok("게임 스케줄 location 업데이트 완료");
    }
    @GetMapping("/test-location/{date}")
    public ResponseEntity<GameSchedule> testLocation(@PathVariable String date) {
        LocalDate gameDate = LocalDate.parse(date);
        return ResponseEntity.ok(
            gameScheduleRepository.findByGameDate(gameDate)
                .stream()
                .findFirst()
                .orElse(null)
        );
    }
    @GetMapping("/null-locations")
    public ResponseEntity<List<GameSchedule>> getNullLocations() {
        List<GameSchedule> nullLocations = gameScheduleRepository.findByLocationIsNullOrLocationEquals("");
        return ResponseEntity.ok(nullLocations);
    }
    
}
