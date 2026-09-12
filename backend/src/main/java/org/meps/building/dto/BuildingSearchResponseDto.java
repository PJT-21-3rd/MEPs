package org.meps.building.dto;

import lombok.*;
import org.meps.hjd.dto.HjdBboxDto;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuildingSearchResponseDto {

    public enum SearchType { BUILDING, REGION, NONE }

    private SearchType searchType;
    private Double targetLat;
    private Double targetLng;
    private String targetBuildingId;
    private String regionNm;
    private Double swLat;
    private Double swLng;
    private Double neLat;
    private Double neLng;

    /** 건물 단일 매칭 */
    public static BuildingSearchResponseDto building(BuildingPointDto building) {
        return BuildingSearchResponseDto.builder()
                .searchType(SearchType.BUILDING)
                .targetLat(building.getLat())
                .targetLng(building.getLng())
                .targetBuildingId(building.getBuildingId())
                .build();
    }

    /** 지역명 매칭 - 프론트는 bbox로 /api/buildings/nearby를 재호출한다 */
    public static BuildingSearchResponseDto region(String regionNm, HjdBboxDto bbox) {
        return BuildingSearchResponseDto.builder()
                .searchType(SearchType.REGION)
                .targetLat(bbox.centerLat())
                .targetLng(bbox.centerLng())
                .regionNm(regionNm)
                .swLat(bbox.getSwLat())
                .swLng(bbox.getSwLng())
                .neLat(bbox.getNeLat())
                .neLng(bbox.getNeLng())
                .build();
    }

    /** 결과 없음 (404가 아닌 200) */
    public static BuildingSearchResponseDto none() {
        return BuildingSearchResponseDto.builder()
                .searchType(SearchType.NONE)
                .build();
    }
}
