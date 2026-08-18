package org.meps.sgg.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SggBriefingResponseDto {
    private String sggName;
    private Integer dailyFlpop;
    private BigDecimal flpopChangeRate;
    private String topIndustryName;
    private Integer totalIndustryCnt;
    private BigDecimal avgBuildingAge;
    private String majorAgeGroup;
    private BigDecimal majorAgeRatio;
    private String overallBriefing;
}
