package org.meps.report.service;

import org.junit.jupiter.api.Test;
import org.meps.fire.dto.FireScoreInput;
import org.meps.fire.dto.FireScoreResult;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 사실 텍스트 조립부 단위 테스트 — LLM 프롬프트에 들어가는 문자열 형식을 고정하는 회귀 가드 */
class SafetyReportServiceTest {

    @Test
    void 화재_사실은_전_축을_슬래시로_나열한다() {
        FireScoreResult fire = FireScoreResult.of(97, FireScoreInput.builder()
                .strctCdNm("철근콘크리트구조")
                .roadSideCodeNm("소로한면")
                .stationNm("강남소방서")
                .stationDistanceM(320.4)
                .dongFireAvgCnt(4.2)
                .build(), 1);

        String facts = SafetyReportService.buildFireFacts(fire);

        assertThat(facts).isEqualTo(
                "주구조: 철근콘크리트구조 / 도로접면: 소로한면(폭 8~12m 도로 접함)"
                        + " / 최근접 소방서: 강남소방서 320m(골든타임 내)"
                        + " / 행정동 최근 3년 평균 화재: 4.2건(서울 행정동 중 하위 25%, 적은 편)");
    }

    @Test
    void 화재_결측_축은_정보_없음으로_표기한다() {
        FireScoreResult fire = FireScoreResult.of(100, FireScoreInput.builder().build(), null);

        String facts = SafetyReportService.buildFireFacts(fire);

        assertThat(facts).isEqualTo(
                "주구조: 정보 없음 / 도로접면: 정보 없음 / 최근접 소방서: 정보 없음"
                        + " / 행정동 최근 3년 평균 화재: 정보 없음");
    }

    @Test
    void 지반침하_사고_없음은_이력_없음으로_표기한다() {
        SinkholeScoreResult sink = SinkholeScoreResult.of(100, List.of());

        assertThat(SafetyReportService.buildSinkFacts(sink))
                .isEqualTo("반경 500m 내 지반침하 사고 이력: 없음");
    }

    @Test
    void 지반침하_사고가_있으면_건수와_최근_사고_시기_거리를_표기한다() {
        // 매퍼 정렬(사고일 내림차순) 전제 — 첫 건이 최근 사고
        SinkholeScoreResult sink = SinkholeScoreResult.of(78, List.of(
                SinkholeIncidentDto.builder().sagoDate("20230401").distanceM(120.6).build(),
                SinkholeIncidentDto.builder().sagoDate("20191115").distanceM(410.0).build()));

        assertThat(SafetyReportService.buildSinkFacts(sink))
                .isEqualTo("반경 500m 내 지반침하 사고 이력: 2건 / 최근 사고: 2023년 4월, 거리 121m");
    }
}
