package org.meps.sinkhole.dto;

import lombok.*;
import org.meps.common.util.SafetyGrade;

import java.util.List;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SinkholeScoreResult {
    private int score; // 70~100 (종합 안전점수 엔진 합산용, 사용자 비공개)
    private SafetyGrade grade; // 점수 구간에서 파생한 배지
    private boolean subsidenceHistory; // 보험 룰 엔진용 SUBSIDENCE_HISTORY 요인 플래그
    private int incidentCount;
    private List<SinkholeIncidentDto> incidents; // 상세 리포트, LLM 브리핑 재료(사고일, 거리)

    public static SinkholeScoreResult of(int score, List<SinkholeIncidentDto> incidents) {
        return SinkholeScoreResult.builder()
                .score(score)
                .grade(SafetyGrade.fromScore(score))
                .subsidenceHistory(!incidents.isEmpty())
                .incidentCount(incidents.size())
                .incidents(incidents)
                .build();
    }
}
