package org.meps.structure.service;

import org.junit.jupiter.api.Test;
import org.meps.common.util.SafetyGrade;
import org.meps.structure.dto.BuildingStructuralInfoDto;
import org.meps.structure.dto.StructuralFactorDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class StructuralStabilityScoreServiceTest {

    private final StructuralStabilityScoreService structuralStabilityScoreService =
            new StructuralStabilityScoreService(null);

    // 최선 조건(2017년 이후 승인·RC·위반없음·지하없음)을 기본값으로 두고 테스트별로 필요한 필드만 덮어쓴다
    private static BuildingStructuralInfoDto.BuildingStructuralInfoDtoBuilder bestCase() {
        return BuildingStructuralInfoDto.builder()
                .bdMgtSn("TEST")
                .useAprDay("20200101")
                .strctCdNm("철근콘크리트구조")
                .violBdYn("N")
                .ugrndFlr(0);
    }

    private StructuralStabilityScoreResultDto score(BuildingStructuralInfoDto info) {
        StructuralStabilityScoreResultDto result = structuralStabilityScoreService.calculateScore(info);
        System.out.println(result);
        result.getFactors().forEach(System.out::println);
        return result;
    }

    @Test
    void 모든_요소가_최선이면_100점_안전이다() {
        StructuralStabilityScoreResultDto result = score(bestCase().build());

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.getFactors()).hasSize(4);
        assertThat(result.getFactors()).allMatch(f -> f.getDeduction() == 0);
    }

    @Test
    void 사용승인일이_내진기준_강화_시점별로_단계적으로_감점된다() {
        // 1988.06.01 / 2005.07.01 / 2017.02.04 경계로 15/10/5/0점 감점
        int pre1988 = score(bestCase().useAprDay("19880531").build()).getScore();
        int era1988 = score(bestCase().useAprDay("19880601").build()).getScore();
        int era2005 = score(bestCase().useAprDay("20050701").build()).getScore();
        int era2017 = score(bestCase().useAprDay("20170204").build()).getScore();

        assertThat(pre1988).isEqualTo(85);  // 100 - 15
        assertThat(era1988).isEqualTo(90);  // 100 - 10
        assertThat(era2005).isEqualTo(95);  // 100 - 5
        assertThat(era2017).isEqualTo(100); // 100 - 0
    }

    @Test
    void 경계_바로_이전_날짜는_한_단계_낮은_구간으로_취급된다() {
        // 2005-06-30은 2005.07.01 기준 미달 → 1988~2005 구간(-10)
        StructuralStabilityScoreResultDto result = score(bestCase().useAprDay("20050630").build());

        assertThat(result.getScore()).isEqualTo(90);
    }

    @Test
    void 사용승인일이_없거나_형식이_이상하면_결측으로_처리되어_감점이_없다() {
        int missing = score(bestCase().useAprDay(null).build()).getScore();
        int malformedLength = score(bestCase().useAprDay("1961").build()).getScore();

        assertThat(missing).isEqualTo(100);
        assertThat(malformedLength).isEqualTo(100);
    }

    @Test
    void 사용승인일_더미값_11111111은_1988년_이전으로_오분류되지_않는다() {
        // 길이는 8이라 파싱은 성공하지만 연도가 1900 미만이라 결측으로 처리해야 한다
        StructuralStabilityScoreResultDto result = score(bestCase().useAprDay("11111111").build());

        assertThat(result.getScore()).isEqualTo(100);
        StructuralFactorDto dateFactor = factor(result, "USE_APR_DAY");
        assertThat(dateFactor.getDeduction()).isZero();
    }

    @Test
    void 주구조가_RC_SRC_철골_계열이면_감점이_없다() {
        for (String code : new String[]{
                "철근콘크리트구조", "철골철근콘크리트구조", "일반철골구조", "경량철골구조", "라멘조"
        }) {
            int score = score(bestCase().strctCdNm(code).build()).getScore();
            assertThat(score).as("strctCdNm=%s", code).isEqualTo(100);
        }
    }

    @Test
    void 주구조가_조적조_계열이면_최대_12점_감점된다() {
        for (String code : new String[]{"벽돌구조", "블록구조", "시멘트블럭조", "기타조적구조", "석구조"}) {
            int score = score(bestCase().strctCdNm(code).build()).getScore();
            assertThat(score).as("strctCdNm=%s", code).isEqualTo(88); // 100 - 12
        }
    }

    @Test
    void 주구조가_목구조이면_6점_감점된다() {
        StructuralStabilityScoreResultDto result = score(bestCase().strctCdNm("일반목구조").build());

        assertThat(result.getScore()).isEqualTo(94); // 100 - 6
    }

    @Test
    void 주구조가_표준코드에_없거나_없으면_미분류로_8점_감점된다() {
        int unknownCode = score(bestCase().strctCdNm("존재하지않는코드").build()).getScore();
        int nullCode = score(bestCase().strctCdNm(null).build()).getScore();

        assertThat(unknownCode).isEqualTo(92); // 100 - 8
        assertThat(nullCode).isEqualTo(92);
    }

    @Test
    void 위반건축물이면_8점_감점되고_N이나_NULL이면_감점이_없다() {
        int violated = score(bestCase().violBdYn("Y").build()).getScore();
        int notViolated = score(bestCase().violBdYn("N").build()).getScore();
        int nullFlag = score(bestCase().violBdYn(null).build()).getScore();

        assertThat(violated).isEqualTo(92); // 100 - 8
        assertThat(notViolated).isEqualTo(100);
        assertThat(nullFlag).isEqualTo(100);
    }

    @Test
    void 지하층이_있으면_3점_감점되고_0층이나_NULL이면_감점이_없다() {
        int hasBasement = score(bestCase().ugrndFlr(2).build()).getScore();
        int noBasement = score(bestCase().ugrndFlr(0).build()).getScore();
        int nullFlr = score(bestCase().ugrndFlr(null).build()).getScore();

        assertThat(hasBasement).isEqualTo(97); // 100 - 3
        assertThat(noBasement).isEqualTo(100);
        assertThat(nullFlr).isEqualTo(100);
    }

    @Test
    void 고위험_조건이_모두_겹치면_70점_밑으로_내려가지_않는다() {
        // 15+12+8+3=38 > 클램프 폭 30 → 하한 70으로 수렴
        StructuralStabilityScoreResultDto result = score(
                bestCase()
                        .useAprDay("19700101")
                        .strctCdNm("벽돌구조")
                        .violBdYn("Y")
                        .ugrndFlr(1)
                        .build());

        assertThat(result.getScore()).isEqualTo(70);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.CAUTION);
    }

    @Test
    void 근거_리스트는_요소_식별자와_감점을_모두_담는다() {
        StructuralStabilityScoreResultDto result = score(
                bestCase()
                        .useAprDay("19700101")
                        .strctCdNm("벽돌구조")
                        .violBdYn("Y")
                        .ugrndFlr(1)
                        .build());

        assertThat(result.getFactors())
                .extracting(StructuralFactorDto::getFactor, StructuralFactorDto::getDeduction)
                .containsExactlyInAnyOrder(
                        tuple("USE_APR_DAY", 15),
                        tuple("STRUCTURE_TYPE", 12),
                        tuple("VIOLATION", 8),
                        tuple("UNDERGROUND_FLOOR", 3)
                );
    }

    private static StructuralFactorDto factor(StructuralStabilityScoreResultDto result, String factorName) {
        List<StructuralFactorDto> matches = result.getFactors().stream()
                .filter(f -> f.getFactor().equals(factorName))
                .toList();
        assertThat(matches).hasSize(1);
        return matches.get(0);
    }
}
