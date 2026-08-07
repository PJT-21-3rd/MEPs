package org.meps.report.service;

import org.junit.jupiter.api.Test;
import org.meps.fire.dto.FireScoreInput;
import org.meps.fire.dto.FireScoreResult;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.structure.dto.StructuralFactorDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;

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

    @Test
    void 구조_사실은_4개_요소를_슬래시로_나열한다() {
        StructuralStabilityScoreResultDto struct = StructuralStabilityScoreResultDto.builder()
                .score(70)
                .factors(List.of(
                        factorOf("USE_APR_DAY", "1970-01-01"),
                        factorOf("STRUCTURE_TYPE", "벽돌구조"),
                        factorOf("VIOLATION", "Y"),
                        factorOf("UNDERGROUND_FLOOR", "지하 1층")))
                .build();

        String facts = SafetyReportService.buildStructFacts(struct);

        assertThat(facts).isEqualTo(
                "사용승인일: 1970-01-01 / 주구조: 벽돌구조"
                        + " / 위반건축물 여부: 있음 / 지하층: 지하 1층");
    }

    @Test
    void 구조_위반건축물_N은_없음으로_표기한다() {
        StructuralStabilityScoreResultDto struct = StructuralStabilityScoreResultDto.builder()
                .score(100)
                .factors(List.of(
                        factorOf("USE_APR_DAY", "2020-01-01"),
                        factorOf("STRUCTURE_TYPE", "철근콘크리트구조"),
                        factorOf("VIOLATION", "N"),
                        factorOf("UNDERGROUND_FLOOR", "정보 없음")))
                .build();

        String facts = SafetyReportService.buildStructFacts(struct);

        assertThat(facts).isEqualTo(
                "사용승인일: 2020-01-01 / 주구조: 철근콘크리트구조"
                        + " / 위반건축물 여부: 없음 / 지하층: 정보 없음");
    }

    private static StructuralFactorDto factorOf(String factor, String detail) {
        return StructuralFactorDto.builder().factor(factor).detail(detail).deduction(0).build();
    }

    @Test
    void 침수_이력_없음은_없음으로_표기한다() {
        FloodScoreResultDto flood = FloodScoreResultDto.of(100, List.of());

        assertThat(SafetyReportService.buildFloodFacts(flood))
                .isEqualTo("최근 침수 이력: 없음");
    }

    @Test
    void 침수_이력이_있으면_건수와_최근_이력의_연도_등급_원인을_표기한다() {
        // 매퍼 정렬(연도 내림차순) 전제 — 첫 건이 최근 이력
        FloodScoreResultDto flood = FloodScoreResultDto.of(77, List.of(
                FloodIncidentDto.builder().year("2022").grade(5).sggCd("11380").cause("호우").build(),
                FloodIncidentDto.builder().year("2020").grade(3).sggCd("11380").cause("호우").build()));

        assertThat(SafetyReportService.buildFloodFacts(flood))
                .isEqualTo("최근 침수 이력: 2건 / 최근 침수: 2022년(5등급, 호우)");
    }
}
