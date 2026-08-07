package org.meps.structure.dto;

import lombok.*;
import org.meps.common.util.SafetyGrade;

import java.util.List;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StructuralStabilityScoreResultDto {
    private int score;
    private SafetyGrade grade;
    private List<StructuralFactorDto> factors;

    public static StructuralStabilityScoreResultDto of(int score, List<StructuralFactorDto> factors) {
        return StructuralStabilityScoreResultDto.builder()
                .score(score)
                .grade(SafetyGrade.fromScore(score))
                .factors(factors)
                .build();
    }
}
