package com.ssgpack.ssgfc.game;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;

    /**
     * 전체 구장 리스트 조회
     */
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    /**
     * 팀 키(ex. sk, lg)로 구장 정보 조회
     */
    public Location getByTeamKey(String teamKey) {
        return locationRepository.findByTeamKey(teamKey);
    }

    /**
     * 구장 약칭(ex. 문학, 창원)으로 조회
     */
    public Location getByShortName(String shortName) {
        return locationRepository.findByShortName(shortName);
    }
}
