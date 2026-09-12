package org.meps.sinkhole.dto;

import lombok.*;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SinkholeIncidentDto {
    private String sagoNo;
    private String sagoDate;
    private String sggNm;
    private String bjdNm;
    private double distanceM; // 건물 center ~ 사고 지점 실측 거리(m), 조회 시점 계산
}
