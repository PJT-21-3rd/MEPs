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

    private String buildingId;
    private String bldNm;        // 역삼 스타빌딩
    private String mainPurpsNm;  // 일반음식점
    private String roadAddr;
    private String jibunAddr;

    @JsonRawValue
    private String floors;

    @JsonRawValue
    private String footprint;

    @JsonRawValue
    private String parcelGeom;
}