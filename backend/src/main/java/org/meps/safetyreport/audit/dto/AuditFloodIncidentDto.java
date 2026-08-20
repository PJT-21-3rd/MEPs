package org.meps.safetyreport.audit.dto;

import lombok.*;

/** 침수 이력(연도 단위 집계) — FloodMapper.findIncidentsByBuilding의 전 건물 일괄 버전 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuditFloodIncidentDto {
    private String bdMgtSn;
    private String year;
    private int grade;
}
