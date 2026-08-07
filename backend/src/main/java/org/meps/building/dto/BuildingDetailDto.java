package org.meps.building.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.*;

@Builder
@Setter @Getter
@ToString
@EqualsAndHashCode(of = "buildingId")
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDetailDto {

    // ---- 기본 정보 ----
    private String buildingId;
    private String bldNm;
    private String roadAddr;
    private String jibunAddr;
    private String mainPurpsNm;

    // ---- 면적 ----
    private Double platArea;
    private Double totArea;

    @JsonRawValue
    private String center;

    @JsonRawValue
    private String footprint;

    @JsonRawValue
    private String parcelGeom;

    // ---- 토지 정보 ----
    private BuildingLandDto land;

    // ---- 건축물 정보 ----
    private BuildingStructureDto detail;

    // ---- 층별 현황 ----
    @JsonRawValue
    private String floors;
}