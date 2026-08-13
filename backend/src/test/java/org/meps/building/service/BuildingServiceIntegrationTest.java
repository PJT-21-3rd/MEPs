package org.meps.building.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.building.dto.NearbyBuildingDto;
import org.meps.building.dto.NearbyBuildingsResponseDto;
import org.meps.building.dto.SortType;
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
    @DisplayName("줌 14 이상이면 건물 목록을 최대 20개 반환한다")
    void returns_up_to_20_buildings_when_zoom_is_at_least_14() {
        // 광진구 자양동·구의동 일대 (데이터 존재 영역)
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 14, SortType.POPULAR);

        assertThat(result.isZoomRequired()).isFalse();
        assertThat(result.getBuildings()).isNotEmpty();
        assertThat(result.getBuildings()).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    @DisplayName("줌이 부족하면 빈 배열과 zoomRequired true를 반환한다")
    void returns_empty_list_with_zoom_required_when_zoom_is_insufficient() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 13, SortType.POPULAR);

        assertThat(result.isZoomRequired()).isTrue();
        assertThat(result.getBuildings()).isEmpty();
    }

    @Test
    @DisplayName("최신순 정렬은 사용승인일 내림차순이고 null은 맨 뒤로 간다")
    void latest_sort_orders_by_use_apr_day_desc_with_nulls_last() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 17, SortType.LATEST);

        List<NearbyBuildingDto> buildings = result.getBuildings();
        assertThat(buildings).isNotEmpty();

        boolean nullSeen = false;
        for (int i = 0; i < buildings.size(); i++) {
            String day = buildings.get(i).getUseAprDay();
            if (day == null) {
                nullSeen = true;
                continue;
            }
            // null 사용승인일이 나온 뒤에는 non-null이 다시 나오면 안 된다
            assertThat(nullSeen).isFalse();
            if (i > 0 && buildings.get(i - 1).getUseAprDay() != null) {
                // YYYYMMDD 문자열이라 문자열 비교 = 날짜 비교
                assertThat(day).isLessThanOrEqualTo(buildings.get(i - 1).getUseAprDay());
            }
        }
    }

    @Test
    @DisplayName("최신순 정렬에서 사용승인일이 같으면 건물관리번호 오름차순으로 정렬된다")
    void latest_sort_breaks_ties_by_building_id_ascending() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 17, SortType.LATEST);

        List<NearbyBuildingDto> buildings = result.getBuildings();
        for (int i = 1; i < buildings.size(); i++) {
            NearbyBuildingDto prev = buildings.get(i - 1);
            NearbyBuildingDto curr = buildings.get(i);
            if (prev.getUseAprDay() != null && prev.getUseAprDay().equals(curr.getUseAprDay())) {
                assertThat(curr.getBuildingId()).isGreaterThan(prev.getBuildingId());
            }
        }
    }

    @Test
    @DisplayName("찜많은순 정렬도 최대 20개를 정상 반환한다")
    void popular_sort_returns_buildings_normally() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 17, SortType.POPULAR);

        assertThat(result.isZoomRequired()).isFalse();
        assertThat(result.getBuildings()).isNotEmpty();
        assertThat(result.getBuildings()).hasSizeLessThanOrEqualTo(20);
    }

    @Test
    @DisplayName("면적순 정렬은 건축면적 내림차순이고 null은 맨 뒤로 간다")
    void area_sort_orders_by_arch_area_desc_with_nulls_last() {
        NearbyBuildingsResponseDto result = buildingService.getNearbyBuildings(
                37.5250, 127.0550, 37.5450, 127.1000, 17, SortType.AREA);

        List<NearbyBuildingDto> buildings = result.getBuildings();
        assertThat(buildings).isNotEmpty();
        assertThat(buildings).hasSizeLessThanOrEqualTo(20);
        for (int i = 1; i < buildings.size(); i++) {
            Double prev = buildings.get(i - 1).getArchArea();
            Double curr = buildings.get(i).getArchArea();
            // 건축면적 null이 나온 뒤에는 non-null이 다시 나오면 안 된다
            if (prev == null) {
                assertThat(curr).isNull();
                continue;
            }
            if (curr != null) {
                assertThat(curr).isLessThanOrEqualTo(prev);
            }
        }
    }

    @Test
    void sw좌표가_ne좌표보다_크면_예외가_발생한다() {
        assertThatThrownBy(() -> buildingService.getNearbyBuildings(
                37.5450, 127.1000, 37.5250, 127.0550, 17, SortType.POPULAR))
                .isInstanceOf(InvalidBoundsException.class);
    }

    @Test
    void 한반도_범위를_벗어난_좌표는_예외가_발생한다() {
        assertThatThrownBy(() -> buildingService.getNearbyBuildings(
                20.0, 100.0, 21.0, 101.0, 17, SortType.POPULAR))
                .isInstanceOf(InvalidBoundsException.class);
    }
}
