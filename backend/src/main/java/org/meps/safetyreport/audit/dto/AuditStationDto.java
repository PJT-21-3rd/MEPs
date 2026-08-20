package org.meps.safetyreport.audit.dto;

import lombok.*;

/** 소방서/119안전센터 좌표 — 최근접 거리 계산용 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuditStationDto {
    private double lat;
    private double lng;
}
