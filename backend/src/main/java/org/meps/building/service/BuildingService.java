package org.meps.building.service;

import lombok.RequiredArgsConstructor;
import org.meps.building.dto.BuildingDetailDto;
import org.meps.building.dto.NearbyBuildingsResponseDto;
import org.meps.building.exception.BuildingNotFoundException;
import org.meps.building.exception.InvalidBoundsException;
import org.meps.building.exception.InvalidBuildingIdException;
import org.meps.building.mapper.BuildingMapper;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private static final int MIN_ZOOM = 17;
    private static final int MAX_RESULTS = 20;
    private final BuildingMapper buildingMapper;
    private static final Pattern BUILDING_ID_PATTERN = Pattern.compile("\\d{25}");

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

    public BuildingDetailDto getBuildingDetail(String buildingId) {
        validateBuildingId(buildingId);

        BuildingDetailDto detail = buildingMapper.findBuildingDetail(buildingId);
        if (detail == null) {
            throw new BuildingNotFoundException(buildingId);
        }
        return detail;
    }

    /** 지도에서 건물 클릭 → 좌표로 상세 조회 */
    public BuildingDetailDto getBuildingDetailAt(double lat, double lng) {
        BuildingDetailDto detail = buildingMapper.findBuildingDetailAt(lat, lng);
        if (detail == null) {
            throw new BuildingNotFoundException("좌표에 건물 없음: " + lat + ", " + lng);
        }
        return detail;
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

    /** 명세 Fail 케이스: buildingId 형식 오류 → 400 */
    private void validateBuildingId(String buildingId) {
        if (buildingId == null || !BUILDING_ID_PATTERN.matcher(buildingId).matches()) {
            throw new InvalidBuildingIdException(buildingId);
        }
    }
}