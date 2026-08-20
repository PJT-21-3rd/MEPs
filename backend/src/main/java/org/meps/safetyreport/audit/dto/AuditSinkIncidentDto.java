package org.meps.safetyreport.audit.dto;

import lombok.*;

/** 지반침하 사고 이력 — SinkholeMapper.findIncidentsByBuilding의 전 건물 일괄 버전 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuditSinkIncidentDto {
    private String bdMgtSn;
    private String sagoDate;
    private double distanceM;
}
