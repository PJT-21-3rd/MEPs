package org.meps.report.controller;

import lombok.RequiredArgsConstructor;
import org.meps.report.dto.BasicReportResponseDto;
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

    /**
     * AI 안심 진단 기본 리포트 조회 (종합 스코어·등급 + AI 핵심 브리핑 5문장)
     */
    @GetMapping("/{buildingId}/safety-report/basic")
    public BasicReportResponseDto basicReport(@PathVariable String buildingId) {
        return safetyReportService.getBasicReport(buildingId);
    }
}
