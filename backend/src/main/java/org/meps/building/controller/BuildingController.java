package org.meps.building.controller;

import lombok.RequiredArgsConstructor;
import org.meps.building.dto.NearbyBuildingsResponseDto;
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
}