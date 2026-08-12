package org.meps.safetyreport.dto;

import lombok.*;

/** 기본 리포트 진단 근거 1개 항목. code = STRUCTURE|FIRE|SINKHOLE|FLOOD, status = 등급 enum명 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BasicFactorDto {
    private String code;
    private String status;
    private String briefing;
}
