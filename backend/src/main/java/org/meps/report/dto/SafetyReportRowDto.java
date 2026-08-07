package org.meps.report.dto;

import lombok.*;

/** building_safety_report 캐시 행 (기본 리포트가 쓰는 컬럼만). brief는 LLM 실패 시 NULL일 수 있다 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SafetyReportRowDto {
    private String bdMgtSn;
    private String aiModelNm;
    private int totalScore;
    private int floodScore;
    private int sinkScore;
    private int fireScore;
    private int structScore;
    private String totalBrief;
    private String floodBrief;
    private String sinkBrief;
    private String fireBrief;
    private String structBrief;
}
