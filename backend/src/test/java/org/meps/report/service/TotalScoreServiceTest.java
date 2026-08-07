package org.meps.report.service;

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
    void 모든_요소가_70점이면_종합점수도_70점이다() {
        int total = totalScoreService.calculateTotalScore(70, 70, 70, 70);

        assertThat(total).isEqualTo(70);
    }

    @Test
    void 구조점수만_만점이면_가중치_35퍼센트만큼_반영된다() {
        // 70×0.65 + 100×0.35 = 80.5 → 반올림 81
        int total = totalScoreService.calculateTotalScore(100, 70, 70, 70);

        assertThat(total).isEqualTo(81);
    }

    @Test
    void 화재점수만_만점이면_가중치_30퍼센트만큼_반영된다() {
        // 70×0.70 + 100×0.30 = 79
        int total = totalScoreService.calculateTotalScore(70, 100, 70, 70);

        assertThat(total).isEqualTo(79);
    }

    @Test
    void 지반침하점수만_만점이면_가중치_15퍼센트만큼_반영된다() {
        // 70×0.85 + 100×0.15 = 74.5 → 반올림 75
        int total = totalScoreService.calculateTotalScore(70, 70, 100, 70);

        assertThat(total).isEqualTo(75);
    }

    @Test
    void 침수점수만_만점이면_가중치_20퍼센트만큼_반영된다() {
        // 70×0.80 + 100×0.20 = 76
        int total = totalScoreService.calculateTotalScore(70, 70, 70, 100);

        assertThat(total).isEqualTo(76);
    }

    @Test
    void 요소별_만점_기여도는_가중치_순서를_따른다() {
        int structOnly = totalScoreService.calculateTotalScore(100, 70, 70, 70);
        int fireOnly = totalScoreService.calculateTotalScore(70, 100, 70, 70);
        int floodOnly = totalScoreService.calculateTotalScore(70, 70, 70, 100);
        int sinkOnly = totalScoreService.calculateTotalScore(70, 70, 100, 70);

        assertThat(structOnly).isGreaterThan(fireOnly);
        assertThat(fireOnly).isGreaterThan(floodOnly);
        assertThat(floodOnly).isGreaterThan(sinkOnly);
    }

    @Test
    void 서로_다른_점수의_가중합을_반올림해서_반환한다() {
        // 85×0.35 + 90×0.30 + 100×0.15 + 95×0.20 = 90.75 → 반올림 91
        int total = totalScoreService.calculateTotalScore(85, 90, 100, 95);

        assertThat(total).isEqualTo(91);
    }
}
