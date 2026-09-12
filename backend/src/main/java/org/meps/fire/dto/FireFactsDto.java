package org.meps.fire.dto;

import lombok.*;

/** 화재 점수 계산에 필요한 건물 개별 사실 (buildings 캐시 칼럼) */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FireFactsDto {
    private String strctCdNm; // 주구조 (표제부 캐시)
    private String roadSideCodeNm; // 도로접면 (토지특성 캐시)
    private String hjdCd; // 행정동 코드 - fire 테이블 조인 키
}
