package org.meps.building.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.building.dto.NearbyBuildingsResponseDto;
import org.meps.building.exception.InvalidBoundsException;
import org.meps.config.RootConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class BuildingServiceIntegrationTest {

    @Autowired
    private BuildingService buildingService;

    @Test
    void 줌17_이상이면_건물목록을_최대20개_반환한다() {
        // 광진구 자양동·구의동 일대 (데이터 존재 영역)
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 17);

        assertThat(result.isZoomRequired()).isFalse();
        assertThat(result.getBuildings()).isNotEmpty();
        assertThat(result.getBuildings()).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    void 줌이_부족하면_빈배열과_zoomRequired_true를_반환한다() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 16);

        assertThat(result.isZoomRequired()).isTrue();
        assertThat(result.getBuildings()).isEmpty();
    }

    @Test
    void sw좌표가_ne좌표보다_크면_예외가_발생한다() {
        assertThatThrownBy(() -> buildingService.getNearbyBuildings(
                37.5450, 127.1000, 37.5250, 127.0550, 17))
                .isInstanceOf(InvalidBoundsException.class);
    }

    @Test
    void 한반도_범위를_벗어난_좌표는_예외가_발생한다() {
        assertThatThrownBy(() -> buildingService.getNearbyBuildings(
                20.0, 100.0, 21.0, 101.0, 17))
                .isInstanceOf(InvalidBoundsException.class);
    }
}
