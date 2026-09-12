package org.meps.building.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.meps.building.dto.BuildingPointDto;
import org.meps.building.dto.BuildingSearchResponseDto;
import org.meps.building.exception.InvalidKeywordException;
import org.meps.building.mapper.BuildingMapper;
import org.meps.common.geocoding.GeocodeResult;
import org.meps.common.geocoding.VWorldGeocodingClient;
import org.meps.hjd.dto.HjdBboxDto;
import org.meps.hjd.dto.HjdNavigateResponseDto;
import org.meps.hjd.service.HjdService;
import org.springframework.stereotype.Service;

/**
 * 통합 검색 (검색어 위치 해석)
 *
 * 해석 순서:
 *  1) 지역명 매칭(행정동/시군구 직접 매칭 → 법정동 폴백) → REGION
 *  2) 건물명 전방일치 매칭 → BUILDING ("63빌딩"처럼 숫자 포함 건물명도 있어 항상 시도)
 *  3) V-World 지오코딩(지번/도로명) → PNU 정확 매칭, 실패 시 최근접 건물 → BUILDING
 *  4) 어디에도 매칭 안 되면 NONE (200 - 404 아님)
 *
 * 지역 단위 입력("서울시 1" 등)이 3)까지 흘러와도 지오코딩 클라이언트가
 * 강제 완성 응답을 자체 검증으로 걸러 null을 반환하므로 NONE으로 수렴한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildingSearchService {

    private final BuildingMapper buildingMapper;
    private final HjdService hjdService;
    private final VWorldGeocodingClient geocodingClient;

    public BuildingSearchResponseDto search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            throw new InvalidKeywordException();
        }
        String trimmed = keyword.trim();

        BuildingSearchResponseDto region = tryRegion(trimmed);
        if (region != null) {
            return region;
        }

        BuildingPointDto byName = buildingMapper.findByName(trimmed);
        if (byName != null) {
            return BuildingSearchResponseDto.building(byName);
        }

        GeocodeResult geo = geocodingClient.geocode(trimmed);
        if (geo == null) {
            return BuildingSearchResponseDto.none();
        }

        BuildingPointDto building = geo.getPnu() != null
                ? buildingMapper.findByPnu(geo.getPnu())
                : null;
        if (building == null) {
            building = buildingMapper.findNearest(geo.getLat(), geo.getLng());
        }

        return building != null
                ? BuildingSearchResponseDto.building(building)
                : BuildingSearchResponseDto.none();
    }

    /**
     * 지역명 매칭.
     * 1차: 검색어 마지막 토큰("서울 광진구 군자동" → "군자동")으로 행정동 전방일치, 시군구명 직접 매칭.
     * 2차(법정동 폴백): 행정동명과 다른 법정동명(역삼동, 인사동 등)은 지역 지오코딩으로
     * 법정동 내부 좌표를 얻어, 그 좌표가 속한 행정동(역삼동 → 역삼1동)의 bbox로 매핑한다.
     */
    private BuildingSearchResponseDto tryRegion(String keyword) {
        String[] tokens = keyword.split("\\s+");
        String candidate = tokens[tokens.length - 1];

        HjdBboxDto bbox = hjdService.findRegionBbox(candidate);
        if (bbox != null) {
            log.debug("지역명 직접 매칭: candidate={}", candidate);
            return BuildingSearchResponseDto.region(candidate, bbox);
        }

        if (!candidate.endsWith("동") && !candidate.endsWith("가")) {
            return null;
        }

        // 전체 검색어로 지오코딩해 동명 중복(강남구 신사동/은평구 신사동)을 구분하고,
        // 타 시도의 동명 중복(대전 동구 자양동 등)으로 빠지지 않도록 서비스 범위인 서울로 한정한다
        String scoped = keyword.contains("서울") ? keyword : "서울특별시 " + keyword;
        GeocodeResult geo = geocodingClient.geocodeRegion(scoped);
        if (geo == null) {
            return null;
        }
        HjdNavigateResponseDto hjd = hjdService.findRegionAt(geo.getLat(), geo.getLng());
        if (hjd == null) {
            return null; // 경계 데이터 밖(서울 외 지역 등)
        }
        HjdBboxDto hjdBbox = hjdService.findBboxByHjdCd(hjd.getHjdCode());
        if (hjdBbox == null) {
            return null;
        }

        log.debug("법정동 폴백 매칭: keyword={} → 행정동 {}({})", keyword, hjd.getHjdName(), hjd.getHjdCode());
        return BuildingSearchResponseDto.region(hjd.getHjdName(), hjdBbox);
    }
}
