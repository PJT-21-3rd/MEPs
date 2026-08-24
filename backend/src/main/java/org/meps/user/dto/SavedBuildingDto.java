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
    private Double lat;
    private Double lng;
    private Integer safetyScore;
    private String safetyGrade;
    private String mainPurpsNm;
    private Integer grndFlr;
    private Integer ugrndFlr;
    private String useAprDay;
    private boolean saved; // 로그인 사용자 기준 찜 여부 (비로그인은 항상 false)
}
