package org.meps.report.dto;

import lombok.*;

/** 상세 리포트 팩터별 세부 근거 1건 (명세: factors[i].details[j] — label/value/source) */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailDto {
    private String label;
    private String value;
    private String source;
}
