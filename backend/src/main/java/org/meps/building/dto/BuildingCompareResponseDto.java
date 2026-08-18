package org.meps.building.dto;

import lombok.*;

import java.util.List;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuildingCompareResponseDto {
    private List<BuildingCompareItemDto> buildings;
}
