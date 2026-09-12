package org.meps.building.dto;

import lombok.*;

/** 검색 매칭용 건물 최소 정보 (건물관리번호 + 중심 좌표) */
@Setter @Getter
@ToString
@NoArgsConstructor
public class BuildingPointDto {
    private String buildingId;
    private Double lat;
    private Double lng;
}
