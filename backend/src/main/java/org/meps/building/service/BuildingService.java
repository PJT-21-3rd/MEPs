package org.meps.building.service;

import lombok.RequiredArgsConstructor;
import org.meps.building.dto.NearbyBuildingsResponseDto;
import org.meps.building.exception.InvalidBoundsException;
import org.meps.building.mapper.BuildingMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private static final int MIN_ZOOM = 17;
    private static final int MAX_RESULTS = 20;
    private final BuildingMapper buildingMapper;

    public NearbyBuildingsResponseDto getNearbyBuildings(
            double swLat, double swLng, double neLat, double neLng, int zoom) {

        validateBounds(swLat, swLng, neLat, neLng);

        // 줌 부족 시 DB 조회 없이 즉시 반환
        if (zoom < MIN_ZOOM) {
            return NearbyBuildingsResponseDto.zoomRequired();
        }

        return NearbyBuildingsResponseDto.of(
                buildingMapper.findBuildingsInBounds(swLat, swLng, neLat, neLng, MAX_RESULTS));
    }

    /** 명세 Fail 케이스: 좌표 파라미터 범위 오류(sw > ne 등) → 400 */
    private void validateBounds(double swLat, double swLng, double neLat, double neLng) {
        if (swLat >= neLat || swLng >= neLng) {
            throw new InvalidBoundsException("sw 좌표는 ne 좌표보다 작아야 합니다.", swLat, swLng, neLat, neLng);
        }
        if (swLat < 33 || neLat > 43 || swLng < 124 || neLng > 132) {
            throw new InvalidBoundsException("좌표가 유효 범위를 벗어났습니다.", swLat, swLng, neLat, neLng);
        }
    }
}