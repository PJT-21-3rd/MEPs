package org.meps.hjd.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.hjd.dto.HjdNavigateResponseDto;
import org.meps.hjd.exception.HjdNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class HjdServiceIntegrationTest {

    @Autowired
    private HjdService hjdService;

    @Test
    void 서울_시청_좌표는_행정동을_찾는다() {
        HjdNavigateResponseDto result = hjdService.findByCoordinate(37.5665, 126.9780);

        System.out.println("hjdCode = " + result.getHjdCode());
        System.out.println("hjdName = " + result.getHjdName());

        assertThat(result.getHjdCode()).isNotBlank();
        assertThat(result.getHjdName()).isNotBlank();
    }

    @Test
    void 서울_밖_좌표는_예외가_발생한다() {
        // 부산 해운대 좌표
        assertThatThrownBy(() -> hjdService.findByCoordinate(35.1587, 129.1604))
                .isInstanceOf(HjdNotFoundException.class);
    }
}