package org.meps.safetyreport.audit.dto;

import lombok.*;

/**
 * safety_score_audit 적재 행 — 4개 축 점수·종합 점수와 원인 분석용 입력 사실.
 * total_grade는 테이블의 생성 컬럼이 점수에서 파생하므로 여기서는 다루지 않는다.
 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ScoreAuditRowDto {
    private String bdMgtSn;
    private int totalScore;
    private int structScore;
    private int fireScore;
    private int sinkScore;
    private int floodScore;
    private String useAprDay;
    private String strctCdNm;
    private String violBdYn;
    private Integer ugrndFlr;
    private String roadSideCodeNm;
    private Double stationDistM;
    private Double dongFireAvg;
    private Integer dongFireQuartile;
    private int sinkCnt;
    private int floodCnt;
}
