package org.meps.building.dto;

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

}