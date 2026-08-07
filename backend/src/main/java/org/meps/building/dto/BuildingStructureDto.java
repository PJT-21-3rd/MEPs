package org.meps.building.dto;

import lombok.*;

@Builder
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuildingStructureDto {

    private String strctCdNm;
    private Integer grndFlr;
    private Integer ugrndFlr;
    private Double archArea;
    private Double heit;
    private Integer hoCnt;
    private String useAprDay;
    private String violBdYn;
}
