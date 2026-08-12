package org.meps.safetyreport.controller;

import lombok.RequiredArgsConstructor;
import org.meps.common.auth.LoginRequiredException;
import org.meps.common.auth.LoginUser;
import org.meps.safetyreport.dto.BasicReportResponseDto;
import org.meps.safetyreport.dto.DetailedReportResponseDto;
import org.meps.safetyreport.service.DetailedReportService;
import org.meps.safetyreport.service.BasicReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class SafetyReportController {

    private final BasicReportService basicReportService;
    private final DetailedReportService detailedReportService;

    /**
     * AI 안심 진단 기본 리포트 조회 (종합 스코어·등급 + AI 핵심 브리핑 5문장).
     * 비로그인도 허용하되 factors(항목별 등급·브리핑)는 로그인 사용자에게만 노출
     */
    @GetMapping("/{buildingId}/safety-report/basic")
    public BasicReportResponseDto getBasicReport(@PathVariable String buildingId, @LoginUser Integer userId) {
        return basicReportService.getBasicReport(buildingId,  userId != null);
    }

    /**
     * AI 안심 진단 상세 리포트 조회 (종합 AI 리포트 + 항목별 해석·세부 근거 데이터) — 로그인 필수
     */
    @GetMapping("/{buildingId}/safety-report/detailed")
    public DetailedReportResponseDto getDetailedReport(@PathVariable String buildingId,
                                                       @LoginUser Integer userId){
        if (userId == null) {
            throw new LoginRequiredException("상세 리포트는 로그인 필수. buildingId=" + buildingId);
        }
        return detailedReportService.getDetailedReport(buildingId);
    }
}
