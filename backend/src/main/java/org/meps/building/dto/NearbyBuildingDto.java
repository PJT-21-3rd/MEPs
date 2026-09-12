package org.meps.building.dto;

import lombok.*;

@Builder
@Setter @Getter
@ToString
@EqualsAndHashCode(of = "buildingId")
@NoArgsConstructor
@AllArgsConstructor
public class NearbyBuildingDto {
    private String buildingId;
    private Double lat;
    private Double lng;
    private String jibunAddr;
    private String roadAddr;
    private String bldNm;
    private String mainPurpsNm;
    private Integer grndFlr;
    private Integer ugrndFlr;
    private String useAprDay;
    private Double archArea; // 건축면적(m2) - 면적순 정렬 기준
    private boolean saved; // 로그인 사용자 기준 찜 여부 (비로그인은 항상 false)
}