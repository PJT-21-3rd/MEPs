package org.meps.safetyreport.audit.dto;

import lombok.*;

/** 행정동별 최근 3년 평균 화재 건수 — FireMapper.findDongAvgFireCnt의 전체 행정동 일괄 버전 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuditDongFireAvgDto {
    private String hjdCd;
    private Double avgCnt;
}
