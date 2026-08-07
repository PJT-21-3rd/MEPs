package org.meps.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressUtilsTest {

    @Test
    void 끝의_번지를_제거한다() {
        assertThat(AddressUtils.formatJibunAddr("서울특별시 광진구 중곡동 143-130번지"))
                .isEqualTo("서울특별시 광진구 중곡동 143-130");
        assertThat(AddressUtils.formatJibunAddr("서울특별시 광진구 중곡동 18-1번지"))
                .isEqualTo("서울특별시 광진구 중곡동 18-1");
    }

    @Test
    void 산_지번도_번지만_제거한다() {
        assertThat(AddressUtils.formatJibunAddr("서울특별시 광진구 중곡동 산 21-1번지"))
                .isEqualTo("서울특별시 광진구 중곡동 산 21-1");
    }

    @Test
    void 번지가_없으면_그대로_반환한다() {
        assertThat(AddressUtils.formatJibunAddr("서울특별시 광진구 중곡동 143-130"))
                .isEqualTo("서울특별시 광진구 중곡동 143-130");
    }

    @Test
    void 주소_중간의_번지는_건드리지_않는다() {
        // 동명 등에 '번지'가 포함되어도 끝이 아니면 유지
        assertThat(AddressUtils.formatJibunAddr("번지없는마을 12"))
                .isEqualTo("번지없는마을 12");
    }

    @Test
    void 도로명주소_끝의_참고항목_괄호를_제거한다() {
        assertThat(AddressUtils.formatRoadAddr("서울특별시 광진구 용마산로 160 (중곡동)"))
                .isEqualTo("서울특별시 광진구 용마산로 160");
        assertThat(AddressUtils.formatRoadAddr("서울특별시 광진구 용마산로28길 6 (중곡동)"))
                .isEqualTo("서울특별시 광진구 용마산로28길 6");
    }

    @Test
    void 참고항목이_없는_도로명주소는_그대로_반환한다() {
        assertThat(AddressUtils.formatRoadAddr("서울특별시 광진구 용마산로 160"))
                .isEqualTo("서울특별시 광진구 용마산로 160");
    }
}
