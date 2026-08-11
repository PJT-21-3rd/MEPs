package org.meps.building.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.building.dto.NearbyBuildingDto;
import org.meps.building.dto.NearbyBuildingsResponseDto;
import org.meps.building.exception.InvalidBoundsException;
import org.meps.config.RootConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class BuildingServiceIntegrationTest {

    @Autowired
    private BuildingService buildingService;

    @Test
    void 줌10_이상이면_건물목록을_최대20개_반환한다() {
        // 광진구 자양동·구의동 일대 (데이터 존재 영역)
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 10);

        assertThat(result.isZoomRequired()).isFalse();
        assertThat(result.getBuildings()).isNotEmpty();
        assertThat(result.getBuildings()).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    void 줌이_부족하면_빈배열과_zoomRequired_true를_반환한다() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 9);

        assertThat(result.isZoomRequired()).isTrue();
        assertThat(result.getBuildings()).isEmpty();
    }

    @Test
    @DisplayName("건물 목록에 뷰포트 중심으로부터의 거리가 포함된다")
    void buildings_contain_distance_from_viewport_center() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 17);

        assertThat(result.getBuildings()).isNotEmpty();
        for (NearbyBuildingDto building : result.getBuildings()) {
            assertThat(building.getDistanceM()).isNotNull();
            assertThat(building.getDistanceM()).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @DisplayName("건물 목록은 뷰포트 중심에서 가까운 순으로 정렬된다")
    void buildings_are_sorted_by_distance_ascending() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 17);

        List<NearbyBuildingDto> buildings = result.getBuildings();
        assertThat(buildings).isNotEmpty();
        for (int i = 1; i < buildings.size(); i++) {
            assertThat(buildings.get(i).getDistanceM())
                    .isGreaterThanOrEqualTo(buildings.get(i - 1).getDistanceM());
        }
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
