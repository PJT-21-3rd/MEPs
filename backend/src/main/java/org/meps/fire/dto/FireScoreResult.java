package org.meps.fire.dto;

import lombok.*;
import org.meps.common.util.SafetyGrade;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FireScoreResult {
    private int score; // 70~100 (종합 안전점수 엔진 합산용, 사용자 비공개)
    private SafetyGrade grade; // 점수 구간에서 파생한 배지

    // 상세 리포트 details·LLM 브리핑 재료 (사실 표시)
    private String strctCdNm; // NULL이면 "정보 없음"으로 표기
    private String roadSideCodeNm; // NULL이면 "정보 없음"으로 표기
    private String nearestStationNm;
    private Double nearestStationDistanceM; // 직선거리 기준
    private Double dongFireAvgCnt; // 행정동 최근 3년 평균 화재 건수
    private Integer dongFireQuartile; // 서울 행정동 분포 내 사분위(1=하위 25% ~ 4=상위 25%), 결측 시 NULL

    public static FireScoreResult of(int score, FireScoreInput input, Integer dongFireQuartile) {
        return FireScoreResult.builder()
                .score(score)
                .grade(SafetyGrade.fromScore(score))
                .strctCdNm(input.getStrctCdNm())
                .roadSideCodeNm(input.getRoadSideCodeNm())
                .nearestStationNm(input.getStationNm())
                .nearestStationDistanceM(input.getStationDistanceM())
                .dongFireAvgCnt(input.getDongFireAvgCnt())
                .dongFireQuartile(dongFireQuartile)
                .build();
    }
}
