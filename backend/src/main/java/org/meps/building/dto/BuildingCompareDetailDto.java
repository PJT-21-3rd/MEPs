package org.meps.building.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.*;

@Builder
@Setter @Getter
@ToString
@EqualsAndHashCode(of = "buildingId")
@NoArgsConstructor
@AllArgsConstructor
public class BuildingCompareDetailDto {

    // ---- 기본 정보 ----
    private String buildingId;
    private String bldNm;
    private String roadAddr;
    private String jibunAddr;
    private String mainPurpsNm;

    private Double platArea;
    private Double totArea;

    // ---- 토지 정보 ----
    private BuildingLandDto land;

    // ---- 건축물 정보 ----
    private BuildingStructureDto detail;

    @JsonRawValue
    private String floors;
}
