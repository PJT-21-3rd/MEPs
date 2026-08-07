package org.meps.hjd.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class HjdBriefingResponseDto {
    private String sggName;
    private String hjdName;
    private Integer dailyFlpop;
    private BigDecimal flpopChangeRate;
    private String topIndustryName;
    private Integer topIndustryCnt;
    private BigDecimal avgBuildingAge;
    private String majorAgeGroup;
    private BigDecimal majorAgeRatio;
    private String overallBriefing;
}
