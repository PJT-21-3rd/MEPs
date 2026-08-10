package org.meps.report.controller;

import lombok.RequiredArgsConstructor;
import org.meps.report.dto.BasicReportResponseDto;
import org.meps.report.dto.DetailedReportResponseDto;
import org.meps.report.service.DetailedReportService;
import org.meps.report.service.SafetyReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class SafetyReportController {

    private final SafetyReportService safetyReportService;
    private final DetailedReportService detailedReportService;

    /**
     * AI 안심 진단 기본 리포트 조회 (종합 스코어·등급 + AI 핵심 브리핑 5문장)
     */
    @GetMapping("/{buildingId}/safety-report/basic")
    public BasicReportResponseDto getBasicReport(@PathVariable String buildingId) {
        return safetyReportService.getBasicReport(buildingId);
    }

    /**
     * AI 안심 진단 상세 리포트 조회 (종합 AI 리포트 + 항목별 해석·세부 근거 데이터)
     */
    @GetMapping("/{buildingId}/safety-report/detailed")
    public DetailedReportResponseDto getDetailedReport(@PathVariable String buildingId) {
        return detailedReportService.getDetailedReport(buildingId);
    }
}
