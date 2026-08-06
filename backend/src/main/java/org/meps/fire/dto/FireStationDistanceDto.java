package org.meps.fire.dto;

import lombok.*;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FireStationDistanceDto {
    private String stationNm; // 최근접 소방서/119안전센터명 (브리핑 표기용)
    private double distanceM; // 건물 center ~ 소방서 직선거리(m) — 도로거리가 아님, 리포트에 "직선거리 기준" 명시
}
