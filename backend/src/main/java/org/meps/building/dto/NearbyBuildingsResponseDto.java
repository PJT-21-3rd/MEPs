package org.meps.building.dto;

import lombok.*;
import java.util.Collections;
import java.util.List;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class NearbyBuildingsResponseDto {
    private boolean zoomRequired;
    private List<NearbyBuildingDto> buildings;

    /** 줌 부족 응답 */
    public static NearbyBuildingsResponseDto zoomRequired() {
        return NearbyBuildingsResponseDto.builder()
                .zoomRequired(true)
                .buildings(Collections.emptyList())
                .build();
    }

    /** 정상 응답 */
    public static NearbyBuildingsResponseDto of(List<NearbyBuildingDto> buildings) {
        return NearbyBuildingsResponseDto.builder()
                .zoomRequired(false)
                .buildings(buildings)
                .build();
    }
}