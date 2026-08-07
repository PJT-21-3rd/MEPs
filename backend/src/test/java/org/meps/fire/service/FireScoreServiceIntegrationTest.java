package org.meps.fire.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.building.exception.BuildingNotFoundException;
import org.meps.config.RootConfig;
import org.meps.fire.dto.FireScoreResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class FireScoreServiceIntegrationTest {

    // 광진구 중곡동 벽돌구조 + 세로한면(불) — 복합취약 세그먼트 표본
    private static final String BRICK_NARROW_BUILDING = "1121510100100180055000040";
    private static final String UNKNOWN_BUILDING = "0000000000000000000000000";

    @Autowired
    private FireScoreService fireScoreService;

    @Test
    void 조적조_진입불가_건물은_근거_데이터와_함께_감점된_점수를_반환한다() {
        FireScoreResult result = fireScoreService.getFireScore(BRICK_NARROW_BUILDING);

        // 벽돌(4) + 세로(불)(12) = 최소 16 감점 → 84점 이하, 하한 70
        assertThat(result.getScore()).isBetween(70, 84);
        assertThat(result.getStrctCdNm()).isEqualTo("벽돌구조");
        assertThat(result.getRoadSideCodeNm()).isEqualTo("세로한면(불)");
        assertThat(result.getNearestStationNm()).isNotBlank();
        assertThat(result.getNearestStationDistanceM()).isPositive();
        assertThat(result.getDongFireAvgCnt()).isPositive();
        assertThat(result.getDongFireQuartile()).isBetween(1, 4);
    }

    @Test
    void 존재하지_않는_건물이면_예외가_발생한다() {
        assertThatThrownBy(() -> fireScoreService.getFireScore(UNKNOWN_BUILDING))
                .isInstanceOf(BuildingNotFoundException.class);
    }
}
