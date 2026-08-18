package org.meps.building.dto;

import lombok.*;
import org.meps.safetyreport.dto.BasicReportResponseDto;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuildingCompareItemDto {
    private BuildingCompareDetailDto building;
    private BasicReportResponseDto safetyReport;
}
