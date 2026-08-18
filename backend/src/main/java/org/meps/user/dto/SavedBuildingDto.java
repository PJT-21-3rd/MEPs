package org.meps.user.dto;

import lombok.*;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SavedBuildingDto {
    private String buildingId;
    private String bldNm;
    private String roadAddr;
    private String jibunAddr;
    private Integer safetyScore;
    private String safetyGrade;
}
