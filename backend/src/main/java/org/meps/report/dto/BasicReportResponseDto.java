package org.meps.report.dto;

import lombok.*;

import java.util.List;

/**
 * AI 안심 진단 기본 리포트 응답 (명세: GET /api/buildings/{buildingId}/safety-report/basic).
 * factors 순서 고정: 구조 → 화재 → 지반침하 → 침수
 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BasicReportResponseDto {
    private int safetyScore; // 종합 AI 안심 스코어(70~100) — 종합만 점수 노출, 팩터는 등급만
    private String overallStatus; // CAUTION | GOOD | SAFE
    private String overallBriefing;
    private List<FactorBriefingDto> factors;
}
