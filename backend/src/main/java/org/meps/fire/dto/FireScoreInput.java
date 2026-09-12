package org.meps.fire.dto;

import lombok.*;

/**
 * 순수 계산부 입력 - DB 조회 결과를 모아 계산 로직에 주입한다.
 * 참조형(Double)인 필드는 결측 가능: 결측은 "정보 없음"으로 감점하지 않는다(중립).
 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FireScoreInput {
    private String strctCdNm; // 주구조, NULL 가능
    private String roadSideCodeNm; // 도로접면, NULL 가능
    private String stationNm; // 최근접 소방서명
    private Double stationDistanceM; // 최근접 소방서 직선거리(m)
    private Double dongFireAvgCnt; // 소재 행정동 최근 3년 평균 화재 건수, NULL 가능
}
