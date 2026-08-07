package org.meps.report.dto;

import lombok.*;

/** 기본 리포트 진단 근거 1개 항목. code = STRUCTURE|FIRE|SINKHOLE|FLOOD, status = 등급 enum명 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FactorBriefingDto {
    private String code;
    private String status;
    private String briefing;
}
