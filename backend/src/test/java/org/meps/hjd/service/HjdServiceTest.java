package org.meps.hjd.service;

import org.junit.jupiter.api.Test;
import org.meps.hjd.dto.HjdBboxDto;
import org.meps.hjd.dto.HjdBriefingResponseDto;
import org.meps.hjd.dto.HjdNameDto;
import org.meps.hjd.dto.HjdNavigateResponseDto;
import org.meps.hjd.exception.AiBriefingNotAvailableException;
import org.meps.hjd.exception.HjdNotFoundException;
import org.meps.hjd.mapper.HjdMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * getBriefing()의 분기(존재하지 않는 행정동 vs 브리핑 미생성)를 실제 DB 상태와 무관하게 검증한다.
 * Mockito 미사용 프로젝트라 HjdMapper를 스텁으로 직접 구현해 대체한다.
 */
class HjdServiceTest {

    /** findByHjdCd만 원하는 값을 돌려주고, 나머지는 이 테스트에서 쓰지 않는 스텁 */
    private static HjdMapper stubMapper(HjdBriefingResponseDto findByHjdCdResult) {
        return new HjdMapper() {
            @Override
            public List<HjdNameDto> findAllNames() {
                throw new UnsupportedOperationException();
            }

            @Override
            public HjdNavigateResponseDto findByCoordinate(double lat, double lng) {
                throw new UnsupportedOperationException();
            }

            @Override
            public HjdBboxDto findRegionBboxByName(String keyword) {
                throw new UnsupportedOperationException();
            }

            @Override
            public HjdBboxDto findBboxByHjdCd(String hjdCd) {
                throw new UnsupportedOperationException();
            }

            @Override
            public HjdBriefingResponseDto findByHjdCd(String hjdCd) {
                return findByHjdCdResult;
            }
        };
    }

    @Test
    void 행정동_코드_자체가_없으면_HjdNotFoundException() {
        String hjdCd = "99999999";
        HjdService service = new HjdService(stubMapper(null));

        assertThatThrownBy(() -> service.getBriefing(hjdCd))
                .isInstanceOf(HjdNotFoundException.class)
                .hasMessageContaining(hjdCd);
    }

    @Test
    void 행정동은_존재하지만_브리핑이_아직_없으면_AiBriefingNotAvailableException() {
        // hjd_ai_briefing에 행이 아직 없거나(집계 전) ai_brf만 비어 있는 경우 모두
        // findByHjdCd는 null이 아닌 DTO에 overallBriefing만 null로 반환한다 (LEFT JOIN)
        String hjdCd = "11230570";
        HjdBriefingResponseDto notReady = new HjdBriefingResponseDto();
        notReady.setSggName("동대문구");
        notReady.setHjdName("전농2동");
        // overallBriefing은 설정하지 않아 null

        HjdService service = new HjdService(stubMapper(notReady));

        assertThatThrownBy(() -> service.getBriefing(hjdCd))
                .isInstanceOf(AiBriefingNotAvailableException.class)
                .hasMessageContaining(hjdCd);
    }

    @Test
    void 브리핑까지_존재하면_정상_반환() {
        String hjdCd = "11215840";
        HjdBriefingResponseDto ready = new HjdBriefingResponseDto();
        ready.setSggName("강남구");
        ready.setHjdName("역삼1동");
        ready.setOverallBriefing("역삼1동은 유동인구가 많은 상권이에요.");

        HjdService service = new HjdService(stubMapper(ready));

        HjdBriefingResponseDto result = service.getBriefing(hjdCd);

        assertThat(result.getOverallBriefing()).isEqualTo("역삼1동은 유동인구가 많은 상권이에요.");
    }
}
