package org.meps.structure.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.common.util.SafetyGrade;
import org.meps.config.RootConfig;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class StructuralStabilityScoreServiceIntegrationTest {

    // 1973년 승인·벽돌구조·위반건축물·지하1층
    private static final String WORST_CASE_BUILDING = "1121510100100310025000256";
    // 2021년 승인·철근콘크리트구조·위반없음·지하없음
    private static final String BEST_CASE_BUILDING = "1121510100100180043000038";
    // 사용승인일이 NULL
    private static final String UNKNOWN_DATE_BUILDING = "1121510300102460019009290";

    @Autowired
    private StructuralStabilityScoreService structuralStabilityScoreService;

    @Test
    void 고위험_조건이_겹치는_건물은_70점_주의로_수렴한다() {
        StructuralStabilityScoreResultDto result =
                structuralStabilityScoreService.getStructuralStabilityScore(WORST_CASE_BUILDING);

        System.out.println(result);
        result.getFactors().forEach(System.out::println);

        assertThat(result.getScore()).isEqualTo(70);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.CAUTION);
        assertThat(result.getFactors()).hasSize(4);
        assertThat(result.getFactors()).allMatch(f -> f.getDeduction() > 0);
    }

    @Test
    void 최선_조건인_건물은_100점_안전이다() {
        StructuralStabilityScoreResultDto result =
                structuralStabilityScoreService.getStructuralStabilityScore(BEST_CASE_BUILDING);

        System.out.println(result);
        result.getFactors().forEach(System.out::println);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.getFactors()).allMatch(f -> f.getDeduction() == 0);
    }

    @Test
    void 사용승인일이_없는_건물은_해당_요소_감점_없이_점수가_매겨진다() {
        StructuralStabilityScoreResultDto result =
                structuralStabilityScoreService.getStructuralStabilityScore(UNKNOWN_DATE_BUILDING);

        System.out.println(result);
        result.getFactors().forEach(System.out::println);

        assertThat(result.getScore()).isBetween(70, 100);
        assertThat(result.getFactors())
                .filteredOn(f -> f.getFactor().equals("USE_APR_DAY"))
                .allMatch(f -> f.getDeduction() == 0);
    }
}
