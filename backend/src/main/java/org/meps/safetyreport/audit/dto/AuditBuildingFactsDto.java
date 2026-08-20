package org.meps.safetyreport.audit.dto;

import lombok.*;

/**
 * 전수조사 배치가 페이지 단위로 읽는 건물 원천 속성.
 * 구조안정성·화재안정성 점수 입력과 최근접 소방서 거리 계산용 좌표를 한 행에 담는다.
 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuditBuildingFactsDto {
    private String bdMgtSn;
    private String useAprDay;
    private String strctCdNm;
    private String violBdYn;
    private Integer ugrndFlr;
    private String roadSideCodeNm;
    private String hjdCd;
    private double lat;
    private double lng;
}
