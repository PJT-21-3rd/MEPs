package org.meps.flood.dto;

import lombok.*;
import org.meps.common.util.SafetyGrade;

import java.util.List;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FloodScoreResultDto {
    private int score; // 70~100 (종합 안전점수 엔진 합산용)
    private SafetyGrade grade;
    private boolean floodHistory; // 보험 룰 엔진용 FLOOD_HISTORY 요인 플래그
    private int incidentCount; // 침수 발생 연도 수
    private List<FloodIncidentDto> incidents;

    public static FloodScoreResultDto of(int score, List<FloodIncidentDto> incidents) {
        return FloodScoreResultDto.builder()
                .score(score)
                .grade(SafetyGrade.fromScore(score))
                .floodHistory(!incidents.isEmpty())
                .incidentCount(incidents.size())
                .incidents(incidents)
                .build();
    }
}
