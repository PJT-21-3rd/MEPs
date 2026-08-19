package org.meps.safetyreport.dto;

import lombok.*;

/** building_safety_report 저장 행 (기본 리포트가 쓰는 컬럼만). brief는 LLM 실패 시 NULL일 수 있다 */
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
    private String totalReport;
    private String floodReport;
    private String sinkReport;
    private String fireReport;
    private String structReport;

    /**
     * brief 5개 컬럼이 모두 채워진 경우, 브리핑 생성이 끝났다고 판단.
     * 재생성을 생략할지 결정하는 BasicReportService와,
     * 다른 인스턴스의 생성 완료를 기다리며 폴링하는 MysqlReportGenerationCoordinator가
     * 항상 같은 기준을 보도록 행 DTO에 한 번만 정의한다
     */
    public boolean hasAllBriefs() {
        return totalBrief != null
                && structBrief != null
                && fireBrief != null
                && sinkBrief != null
                && floodBrief != null;
    }

    public BasicBriefingDto toBriefingDto() {
        return BasicBriefingDto.builder()
                .totalBrief(totalBrief)
                .structBrief(structBrief)
                .fireBrief(fireBrief)
                .sinkBrief(sinkBrief)
                .floodBrief(floodBrief)
                .build();
    }
}
