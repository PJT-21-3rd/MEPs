package org.meps.flood.service;

import org.junit.jupiter.api.Test;
import org.meps.common.util.SafetyGrade;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.exception.InvalidFloodGradeException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloodScoreServiceTest {

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 8, 1);

    private final FloodScoreService floodScoreService = new FloodScoreService(null);

    private static FloodIncidentDto incident(String year, int grade) {
        return FloodIncidentDto.builder()
                .year(year)
                .grade(grade)
                .sggCd("11110")
                .cause("호우")
                .build();
    }

    @Test
    void 침수_이력이_없으면_100점_안전이고_이력_플래그가_꺼진다() {
        FloodScoreResultDto result = floodScoreService.calculateScore(Collections.emptyList(), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.isFloodHistory()).isFalse();
        assertThat(result.getIncidentCount()).isZero();
    }

    @Test
    void 최고등급_과거_이력_1건이면_75점_주의다() {
        // 기준선(old, ×1.0): w = 1.4(grade5) × 1.0 → 70 + round(30 × 0.27^1.4) = 75
        FloodScoreResultDto result = floodScoreService.calculateScore(
                List.of(incident("2020", 5)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(75);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.CAUTION);
        assertThat(result.isFloodHistory()).isTrue();
        assertThat(result.getIncidentCount()).isEqualTo(1);
    }

    @Test
    void 최고등급_최근_이력은_소폭_가산되어_과거_이력보다_점수가_낮다() {
        // recency는 보조 가중치(×1.1)라 절벽 없이 소폭만 차이난다: old=75, recent=74
        int old = floodScoreService.calculateScore(List.of(incident("2020", 5)), BASE_DATE).getScore();
        int recent = floodScoreService.calculateScore(List.of(incident("2026", 5)), BASE_DATE).getScore();

        assertThat(old).isEqualTo(75);
        assertThat(recent).isEqualTo(74);
        assertThat(recent).isLessThan(old);
        assertThat(old - recent).isLessThanOrEqualTo(2); // 절벽 현상 없음 확인
    }

    @Test
    void 최고등급_최근_이력은_연도가_늘수록_점수가_단조_감소한다() {
        // Σw = 1.54 → 74, Σw = 3.08 → 71, Σw = 4.62 → 70 (마진이 곱으로 깎여 건수가 구분된다)
        FloodIncidentDto y2026 = incident("2026", 5);
        FloodIncidentDto y2025 = incident("2025", 5);
        FloodIncidentDto y2024 = incident("2024", 5);

        int one = floodScoreService.calculateScore(List.of(y2026), BASE_DATE).getScore();
        int two = floodScoreService.calculateScore(List.of(y2026, y2025), BASE_DATE).getScore();
        int three = floodScoreService.calculateScore(List.of(y2026, y2025, y2024), BASE_DATE).getScore();

        assertThat(one).isEqualTo(74);
        assertThat(two).isEqualTo(71);
        assertThat(three).isEqualTo(70);
        assertThat(one).isGreaterThan(two);
        assertThat(two).isGreaterThanOrEqualTo(three);
    }

    @Test
    void 이력이_아무리_누적돼도_70점_밑으로_내려가지_않는다() {
        FloodScoreResultDto result = floodScoreService.calculateScore(
                List.of(incident("2026", 5),
                        incident("2025", 5),
                        incident("2024", 5),
                        incident("2023", 5)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(70);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.CAUTION);
        assertThat(result.getIncidentCount()).isEqualTo(4);
    }

    @Test
    void 최저등급_최근_이력_1건이면_양호_구간이다() {
        // 2026-08 기준 building_flood_map의 최다 실사례 조합: w = 0.5 × 1.1 → 85.
        // 침수 이력이 있으면 안전 등급이 나오지 않는다는 정책 라인의 회귀 가드
        FloodScoreResultDto result = floodScoreService.calculateScore(
                List.of(incident("2022", 1)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(85);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.GOOD);
        assertThat(result.isFloodHistory()).isTrue();
    }

    @Test
    void 등급이_높을수록_같은_최근성에서_점수가_더_낮다() {
        // 1등급만 양호(85), 2등급부터 주의(79 이하) — 등급별 감점 라인의 회귀 가드
        int grade1 = floodScoreService.calculateScore(List.of(incident("2026", 1)), BASE_DATE).getScore();
        int grade2 = floodScoreService.calculateScore(List.of(incident("2026", 2)), BASE_DATE).getScore();
        int grade3 = floodScoreService.calculateScore(List.of(incident("2026", 3)), BASE_DATE).getScore();
        int grade4 = floodScoreService.calculateScore(List.of(incident("2026", 4)), BASE_DATE).getScore();
        int grade5 = floodScoreService.calculateScore(List.of(incident("2026", 5)), BASE_DATE).getScore();

        assertThat(grade1).isEqualTo(85);
        assertThat(grade2).isEqualTo(79);
        assertThat(grade3).isEqualTo(77);
        assertThat(grade4).isEqualTo(75);
        assertThat(grade5).isEqualTo(74);
        assertThat(grade1).isGreaterThan(grade2).isGreaterThan(grade3);
        assertThat(grade3).isGreaterThan(grade4).isGreaterThan(grade5);
    }

    @Test
    void 경계값_정확히_5년_전_연도는_최근으로_취급된다() {
        // baseDate 2026년, RECENT_CYCLE_YEARS=5 → 2021년은 recent(경계 포함) → w = 1.54 → 74점
        FloodScoreResultDto result = floodScoreService.calculateScore(
                List.of(incident("2021", 5)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(74);
    }

    @Test
    void 경계_바로_이전_연도는_과거로_취급되어_기준선_점수를_받는다() {
        // 2020년은 5년 초과 → old(기준선) → w = 1.4 → 75점
        FloodScoreResultDto result = floodScoreService.calculateScore(
                List.of(incident("2020", 5)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(75);
    }

    @Test
    void 정의되지_않은_등급이면_예외를_던진다() {
        assertThatThrownBy(() -> floodScoreService.calculateScore(List.of(incident("2026", 9)), BASE_DATE))
                .isInstanceOf(InvalidFloodGradeException.class)
                .hasMessageContaining("9");
    }
}
