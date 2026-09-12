package org.meps.structure.dto;

import lombok.*;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuildingStructuralInfoDto {

    private String bdMgtSn;
    private String useAprDay;
    private String strctCdNm;
    private String violBdYn;
    private Integer ugrndFlr;
}
