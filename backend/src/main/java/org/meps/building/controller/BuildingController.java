package org.meps.building.controller;

import lombok.RequiredArgsConstructor;
import org.meps.building.dto.BuildingSearchResponseDto;
import org.meps.building.dto.NearbyBuildingsResponseDto;
import org.meps.building.service.BuildingSearchService;
import org.meps.building.service.BuildingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;
    private final BuildingSearchService buildingSearchService;

    /**
     * 위치 기반 매물 리스트 조회
     */
    @GetMapping("/nearby")
    public NearbyBuildingsResponseDto getNearby(
            @RequestParam double swLat,
            @RequestParam double swLng,
            @RequestParam double neLat,
            @RequestParam double neLng,
            @RequestParam int zoom) {

        return buildingService.getNearbyBuildings(swLat, swLng, neLat, neLng, zoom);
    }

    /**
     * 통합 검색 (검색어 위치 해석) - 도로명주소/지번/건물명/지역명
     */
    @GetMapping("/search")
    public BuildingSearchResponseDto search(@RequestParam String keyword) {
        return buildingSearchService.search(keyword);
    }
}