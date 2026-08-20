package org.meps.sinkhole.service;

import org.junit.jupiter.api.Test;
import org.meps.common.util.SafetyGrade;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SinkholeScoreServiceTest {

    private static final LocalDate BASE_DATE = LocalDate.of(2026, 8, 1);

    private final SinkholeScoreService sinkholeScoreService = new SinkholeScoreService(null);

    private static SinkholeIncidentDto incident(String sagoDate, double distanceM) {
        return SinkholeIncidentDto.builder()
                .sagoNo("00000000")
                .sagoDate(sagoDate)
                .distanceM(distanceM)
                .build();
    }

    @Test
    void 사고가_없으면_100점_안전이고_이력_플래그가_꺼진다() {
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(Collections.emptyList(), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.isSubsidenceHistory()).isFalse();
        assertThat(result.getIncidentCount()).isZero();
    }

    @Test
    void 근거리_최근_사고_1건이면_78점_주의다() {
        // w = 1.0 × 1.0 → 70 + round(30 × 0.27) = 78
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20260101", 50.0)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(78);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.CAUTION);
        assertThat(result.isSubsidenceHistory()).isTrue();
        assertThat(result.getIncidentCount()).isEqualTo(1);
    }

    @Test
    void 근거리_최근_사고는_건수가_늘수록_점수가_단조_감소한다() {
        // Σw = 1 → 78, Σw = 2 → 72, Σw = 3 → 71 (마진이 곱으로 깎여 건수가 구분된다)
        SinkholeIncidentDto worst = incident("20260101", 50.0);

        int one = sinkholeScoreService.calculateScore(List.of(worst), BASE_DATE).getScore();
        int two = sinkholeScoreService.calculateScore(List.of(worst, worst), BASE_DATE).getScore();
        int three = sinkholeScoreService.calculateScore(List.of(worst, worst, worst), BASE_DATE).getScore();

        assertThat(one).isEqualTo(78);
        assertThat(two).isEqualTo(72);
        assertThat(three).isEqualTo(71);
        assertThat(one).isGreaterThan(two);
        assertThat(two).isGreaterThan(three);
    }

    @Test
    void 사고가_아무리_누적돼도_70점_밑으로_내려가지_않는다() {
        // 여의도동처럼 근거리·최근 사고가 겹치는 경우 — 하한 70 유지
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20260101", 50.0),
                        incident("20250601", 80.0),
                        incident("20241001", 90.0),
                        incident("20240101", 60.0)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(70);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.CAUTION);
        assertThat(result.getIncidentCount()).isEqualTo(4);
    }

    @Test
    void 근거리_사고도_5년이_지나면_양호로_승급한다() {
        // 법정 공동조사 주기 경과 = 출구: w = 1.0 × 0.7 → 70 + round(30 × 0.27^0.7) = 82
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20200101", 50.0)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(82);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.GOOD);
    }

    @Test
    void 중거리_최근_사고_1건이면_양호_구간이다() {
        // w = 0.65 × 1.0 → 70 + round(30 × 0.27^0.65) = 83 — 근거리·과거(82)보다 위 (거리 우위)
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20260101", 250.0)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(83);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.GOOD);
    }

    @Test
    void 중거리_과거_사고_1건이면_양호_구간이다() {
        // w = 0.65 × 0.7 = 0.455 → 87. 간접 신호 1건만으로는 안전이 나오지 않게 된
        // 2026-08-20 가중치 상향의 의도된 효과 (이전 값 0.5×0.6에서는 90 안전 턱걸이)
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20200101", 250.0)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(87);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.GOOD);
    }

    @Test
    void 원거리_과거_사고_1건이면_감점이_거의_없다() {
        // 300~500m는 위험 근거가 없는 표시 범위: w = 0.3 × 0.7 = 0.21 → 93
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20190301", 499.0)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(93);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.isSubsidenceHistory()).isTrue();
    }

    @Test
    void 경계값_정확히_100m와_5년_전은_가장_높은_가중치_구간에_속한다() {
        // 100m 이내·5년 이내는 경계 포함 → w = 1.0 → 78점
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20210801", 100.0)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(78);
    }

    @Test
    void 경계값_정확히_500m는_원거리_구간에_포함된다() {
        // w = 0.3 × 1.0 → 70 + round(30 × 0.27^0.3) = 90
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20260101", 500.0)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(90);
    }

    @Test
    void 경계_직후는_다음_구간_가중치로_떨어진다() {
        // 100m 초과 → 0.65, 5년 하루 초과 → 0.7 → w = 0.455 → 87점
        SinkholeScoreResult result = sinkholeScoreService.calculateScore(
                List.of(incident("20210731", 100.1)), BASE_DATE);

        assertThat(result.getScore()).isEqualTo(87);
    }
}
