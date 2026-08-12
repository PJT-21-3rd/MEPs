package org.meps.safetyreport.dto;

import lombok.*;

/**
 * AI 상세 리포트 해석 (종합 헤드라인+요약 2단 구성 + 항목별 근거·리스크·솔루션 해석 4개)..
 * LLM 생성 결과 또는 등급별 템플릿 폴백 값
 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DetailedBriefingDto {
    private String totalHeadline;
    private String totalSummary;
    private String structReport;
    private String fireReport;
    private String sinkReport;
    private String floodReport;
}
