package org.meps.report.dto;

import lombok.*;

import java.util.List;

/** 상세 리포트 진단 1개 항목. code = STRUCTURE|FIRE|SINKHOLE|FLOOD, status = 등급 enum명 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FactorReportDto {
    private String code;
    private String status;
    private String aiReport;
    private List<ReportDetailDto> details;
}
