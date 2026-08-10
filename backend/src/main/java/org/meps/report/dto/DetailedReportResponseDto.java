package org.meps.report.dto;

import lombok.*;

import java.util.List;

/**
 * AI 안심 진단 상세 리포트 응답 (명세: GET /api/buildings/{buildingId}/safety-report/detailed).
 * factors 순서 고정: 구조 → 화재 → 지반침하 → 침수
 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DetailedReportResponseDto {
    private String overallAiReport;
    private List<FactorReportDto> factors;
}
