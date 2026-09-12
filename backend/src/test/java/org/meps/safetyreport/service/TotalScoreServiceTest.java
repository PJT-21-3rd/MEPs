package org.meps.safetyreport.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TotalScoreServiceTest {

    private final TotalScoreService totalScoreService = new TotalScoreService();

    @Test
    void 모든_요소가_100점이면_종합점수도_100점이다() {
        int total = totalScoreService.calculateTotalScore(100, 100, 100, 100);

        assertThat(total).isEqualTo(100);
    }

    @Test
    void 모든_요소가_하한_70점이면_클램프로_종합점수도_70점이다() {
        // 차감만으로는 100 − 30×(0.43+0.38+0.29+0.33) = 57.1 — 축 스케일과 같은 70으로 클램프
        int total = totalScoreService.calculateTotalScore(70, 70, 70, 70);

        assertThat(total).isEqualTo(70);
    }

    @Test
    void 한_축이_하한이어도_나머지_만점이_결손을_희석하지_못한다() {
        // 100 − 30×0.43 = 87.1 → 반올림 87. 가중평균 시절에는 같은 입력이 90(안전)으로 올라갔다 —
        // 결손차감 전환의 핵심 목적을 고정하는 회귀 가드
        int total = totalScoreService.calculateTotalScore(70, 100, 100, 100);

        assertThat(total).isEqualTo(87);
    }

    @Test
    void 구조점수만_만점이면_구조_결손만_빠진다() {
        // 100 − 20×(0.38+0.29+0.33) = 100 − 20 = 80
        int total = totalScoreService.calculateTotalScore(100, 80, 80, 80);

        assertThat(total).isEqualTo(80);
    }

    @Test
    void 화재점수만_만점이면_화재_결손만_빠진다() {
        // 100 − 20×(0.43+0.29+0.33) = 100 − 21 = 79
        int total = totalScoreService.calculateTotalScore(80, 100, 80, 80);

        assertThat(total).isEqualTo(79);
    }

    @Test
    void 지반침하점수만_만점이면_지반침하_결손만_빠진다() {
        // 100 − 20×(0.43+0.38+0.33) = 100 − 22.8 = 77.2 → 반올림 77
        int total = totalScoreService.calculateTotalScore(80, 80, 100, 80);

        assertThat(total).isEqualTo(77);
    }

    @Test
    void 침수점수만_만점이면_침수_결손만_빠진다() {
        // 100 − 20×(0.43+0.38+0.29) = 100 − 22 = 78
        int total = totalScoreService.calculateTotalScore(80, 80, 80, 100);

        assertThat(total).isEqualTo(78);
    }

    @Test
    void 요소별_만점_기여도는_계수_서열을_따른다() {
        int structOnly = totalScoreService.calculateTotalScore(100, 80, 80, 80);
        int fireOnly = totalScoreService.calculateTotalScore(80, 100, 80, 80);
        int floodOnly = totalScoreService.calculateTotalScore(80, 80, 80, 100);
        int sinkOnly = totalScoreService.calculateTotalScore(80, 80, 100, 80);

        assertThat(structOnly).isGreaterThan(fireOnly);
        assertThat(fireOnly).isGreaterThan(floodOnly);
        assertThat(floodOnly).isGreaterThan(sinkOnly);
    }

    @Test
    void 서로_다른_점수의_결손_차감을_반올림해서_반환한다() {
        // 100 − (15×0.43 + 10×0.38 + 0×0.29 + 5×0.33) = 100 − 11.9 = 88.1 → 반올림 88
        int total = totalScoreService.calculateTotalScore(85, 90, 100, 95);

        assertThat(total).isEqualTo(88);
    }
}
