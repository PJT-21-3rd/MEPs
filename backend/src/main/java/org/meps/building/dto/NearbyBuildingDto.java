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
}