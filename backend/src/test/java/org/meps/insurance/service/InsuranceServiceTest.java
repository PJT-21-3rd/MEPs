package org.meps.insurance.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.insurance.dto.InsuranceItemDto;
import org.meps.insurance.exception.InvalidFactorException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 맞춤 보험 조회 단위 테스트 — factors 파싱 규칙과 팩터별 안내 항목 매핑 */
class InsuranceServiceTest {

    private final InsuranceService insuranceService = new InsuranceService();

    @Test
    @DisplayName("FLOOD 팩터는 풍수재손해 특약과 풍수해·지진재해보험을 선언 순서대로 반환한다")
    void getInsuranceDetails_returnsFloodItemsInDeclarationOrder() {
        List<InsuranceItemDto> items = insuranceService
                .getInsuranceDetails(List.of("FLOOD")).getItems();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getFactorCode()).isEqualTo("FLOOD");
        assertThat(items.get(0).getName()).isEqualTo("[보험 특약] 풍수재손해");
        assertThat(items.get(0).getCoverageType()).isEqualTo("RIDER");
        assertThat(items.get(1).getName()).isEqualTo("소상공인 풍수해·지진재해보험(Ⅵ)");
        assertThat(items.get(1).getCoverageType()).isEqualTo("PRODUCT");
    }

    @Test
    @DisplayName("FIRE 팩터는 보통약관 기본 보장인 화재손해 1건을 반환한다")
    void getInsuranceDetails_returnsBaseCoverageForFire() {
        List<InsuranceItemDto> items = insuranceService
                .getInsuranceDetails(List.of("FIRE")).getItems();

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getFactorCode()).isEqualTo("FIRE");
        assertThat(items.get(0).getName()).isEqualTo("[보험 기본보장] 화재손해");
        assertThat(items.get(0).getCoverageType()).isEqualTo("BASE");
        assertThat(items.get(0).getCoverageSummary()).isNotBlank();
    }

    @Test
    @DisplayName("쉼표로 구분된 여러 팩터는 요청 순서대로 항목을 나열한다")
    void getInsuranceDetails_parsesCommaSeparatedFactorsInRequestOrder() {
        List<InsuranceItemDto> items = insuranceService
                .getInsuranceDetails(List.of("FIRE,STRUCTURE")).getItems();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getFactorCode()).isEqualTo("FIRE");
        assertThat(items.get(1).getFactorCode()).isEqualTo("STRUCTURE");
    }

    @Test
    @DisplayName("factors 파라미터를 반복해서 보내도 요청 순서를 유지한다")
    void getInsuranceDetails_supportsRepeatedParamsKeepingOrder() {
        List<InsuranceItemDto> items = insuranceService
                .getInsuranceDetails(List.of("SINKHOLE", "STRUCTURE")).getItems();

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getFactorCode()).isEqualTo("SINKHOLE");
        assertThat(items.get(1).getFactorCode()).isEqualTo("STRUCTURE");
    }

    @Test
    @DisplayName("중복된 팩터는 최초 1회만 반영한다")
    void getInsuranceDetails_deduplicatesRepeatedFactors() {
        List<InsuranceItemDto> items = insuranceService
                .getInsuranceDetails(List.of("FLOOD,FLOOD,FLOOD", "FLOOD", "FLOOD")).getItems();

        assertThat(items).hasSize(2); // 풍수재손해 특약, 소상공인 풍수해·지진재해보험(Ⅵ)
    }

    @Test
    @DisplayName("소문자·공백이 섞인 코드도 정규화해서 처리한다")
    void getInsuranceDetails_normalizesLowercaseAndWhitespace() {
        List<InsuranceItemDto> items = insuranceService
                .getInsuranceDetails(List.of(" fire , flood ")).getItems();

        assertThat(items).hasSize(3);
        assertThat(items.get(0).getFactorCode()).isEqualTo("FIRE");
    }

    @Test
    @DisplayName("4개 팩터 전체 요청 시 매핑된 안내 항목 5건을 모두 반환한다")
    void getInsuranceDetails_returnsAllItemsForAllFactors() {
        List<InsuranceItemDto> items = insuranceService
                .getInsuranceDetails(List.of("STRUCTURE,FIRE,SINKHOLE,FLOOD")).getItems();

        assertThat(items).hasSize(5);
    }

    @Test
    @DisplayName("유효하지 않은 진단 요소 코드는 InvalidFactorException을 던진다")
    void getInsuranceDetails_throwsOnUnknownFactorCode() {
        assertThatThrownBy(() -> insuranceService.getInsuranceDetails(List.of("FLOOD,EARTHQUAKE")))
                .isInstanceOf(InvalidFactorException.class)
                .hasMessageContaining("EARTHQUAKE");
    }

    @Test
    @DisplayName("factors가 null이거나 빈 리스트면 InvalidFactorException을 던진다")
    void getInsuranceDetails_throwsOnNullOrEmptyFactors() {
        assertThatThrownBy(() -> insuranceService.getInsuranceDetails(null))
                .isInstanceOf(InvalidFactorException.class);
        assertThatThrownBy(() -> insuranceService.getInsuranceDetails(Collections.emptyList()))
                .isInstanceOf(InvalidFactorException.class);
    }

    @Test
    @DisplayName("공백·쉼표뿐인 factors는 InvalidFactorException을 던진다")
    void getInsuranceDetails_throwsOnBlankFactors() {
        assertThatThrownBy(() -> insuranceService.getInsuranceDetails(Arrays.asList("  ", ",,", null)))
                .isInstanceOf(InvalidFactorException.class);
    }
}
