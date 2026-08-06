package org.meps.building.dto;

import lombok.*;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuildingLandDto {

    private String lndcgrCodeNm;
    private String prposAreaNm;
    private String roadSideCodeNm;
    private Long pblntfPclnd;

}