package org.meps.report.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.fire.dto.FireScoreInput;
import org.meps.fire.dto.FireScoreResult;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.report.dto.ReportDetailDto;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.structure.dto.StructuralFactorDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** 상세 리포트 details 조립부 단위 테스트 — 프론트에 노출되는 label/value/source 형식을 고정하는 회귀 가드 */
class DetailedReportServiceTest {

    @Test
    @DisplayName("구조 details는 룰 엔진 감점 4요소를 건축물대장 출처로 나열한다")
    void buildStructDetails_listsRegisterBackedFactors() {
        StructuralStabilityScoreResultDto struct = structOf(
                buildFactorOf("USE_APR_DAY", "1978-12-29"),
                buildFactorOf("STRUCTURE_TYPE", "철근콘크리트구조"),
                buildFactorOf("VIOLATION", "Y"),
                buildFactorOf("UNDERGROUND_FLOOR", "지하 1층"));

        List<ReportDetailDto> details = DetailedReportService.buildStructDetails(struct);

        assertThat(details).hasSize(4);
        assertBuildDetail(details.get(0), "주구조", "철근콘크리트구조", "건축물대장 표제부");
        assertBuildDetail(details.get(1), "사용승인일", "1978-12-29", "건축물대장 표제부");
        assertBuildDetail(details.get(2), "위반건축물 여부", "있음", "건축물대장 표제부");
        assertBuildDetail(details.get(3), "지하층수", "지하 1층", "건축물대장 표제부");
    }

    @Test
    @DisplayName("구조 위반건축물 N은 없음으로, 결측 factor는 정보 없음으로 표기한다")
    void buildStructDetails_marksMissingBuildFactorAsNoInfo() {
        StructuralStabilityScoreResultDto struct = structOf(
                buildFactorOf("STRUCTURE_TYPE", "벽돌구조"),
                buildFactorOf("VIOLATION", "N"));

        List<ReportDetailDto> details = DetailedReportService.buildStructDetails(struct);

        assertBuildDetail(details.get(1), "사용승인일", "정보 없음", "건축물대장 표제부");
        assertBuildDetail(details.get(2), "위반건축물 여부", "없음", "건축물대장 표제부");
        assertBuildDetail(details.get(3), "지하층수", "정보 없음", "건축물대장 표제부");
    }

    @Test
    @DisplayName("화재 details는 도로접면·주구조·행정동 화재 건수·소방서 거리 4축을 출처와 함께 나열한다")
    void buildFireDetails_listsFourAxesWithSources() {
        FireScoreResult fire = FireScoreResult.of(97, FireScoreInput.builder()
                .strctCdNm("철근콘크리트구조")
                .roadSideCodeNm("광대한면")
                .stationNm("광진소방서")
                .stationDistanceM(850.4)
                .dongFireAvgCnt(12.3333)
                .build(), 4);

        List<ReportDetailDto> details = DetailedReportService.buildFireDetails(fire);

        assertThat(details).hasSize(4);
        assertBuildDetail(details.get(0), "도로접면", "광대한면", "토지특성정보");
        assertBuildDetail(details.get(1), "주구조", "철근콘크리트구조", "건축물대장 표제부");
        assertBuildDetail(details.get(2), "행정동 화재 건수(3년 평균)", "12.3건(서울 상위 25%)", "소방청 화재통계");
        assertBuildDetail(details.get(3), "최근접 소방서 거리", "광진소방서 약 850m", "소방서 위치정보");
    }

    @Test
    @DisplayName("화재 결측 축과 미지정 도로접면은 정보 없음으로 표기한다")
    void buildFireDetails_marksMissingAxesAsNoInfo() {
        FireScoreResult fire = FireScoreResult.of(100, FireScoreInput.builder()
                .roadSideCodeNm("지정되지않음")
                .build(), null);

        List<ReportDetailDto> details = DetailedReportService.buildFireDetails(fire);

        assertBuildDetail(details.get(0), "도로접면", "정보 없음", "토지특성정보");
        assertBuildDetail(details.get(1), "주구조", "정보 없음", "건축물대장 표제부");
        assertBuildDetail(details.get(2), "행정동 화재 건수(3년 평균)", "정보 없음", "소방청 화재통계");
        assertBuildDetail(details.get(3), "최근접 소방서 거리", "정보 없음", "소방서 위치정보");
    }

    @Test
    @DisplayName("지반침하 사고 없음은 세 거리 구간을 모두 없음으로 나열한다")
    void buildSinkDetails_withoutIncidentMarksAllBandsAsNone() {
        SinkholeScoreResult sink = SinkholeScoreResult.of(100, List.of());

        List<ReportDetailDto> details = DetailedReportService.buildSinkDetails(sink);

        assertThat(details).hasSize(3);
        assertBuildDetail(details.get(0), "0m~100m 사고 이력", "없음", "지반침하 사고이력");
        assertBuildDetail(details.get(1), "100m~300m 사고 이력", "없음", "지반침하 사고이력");
        assertBuildDetail(details.get(2), "300m~500m 사고 이력", "없음", "지반침하 사고이력");
    }

    @Test
    @DisplayName("지반침하 사고는 거리 구간별 건수와 구간 내 최근 사고 시기로 표기한다")
    void buildSinkDetails_groupsIncidentsByDistanceBand() {
        // 매퍼 정렬(사고일 내림차순) 전제 — 구간 내 첫 매칭이 최근 사고.
        // 100.0m는 산식(distanceWeight)과 동일하게 0~100m 구간에 포함된다
        SinkholeScoreResult sink = SinkholeScoreResult.of(75, List.of(
                SinkholeIncidentDto.builder().sagoDate("20230401").distanceM(100.0).build(),
                SinkholeIncidentDto.builder().sagoDate("20210805").distanceM(450.0).build(),
                SinkholeIncidentDto.builder().sagoDate("20191115").distanceM(410.0).build()));

        List<ReportDetailDto> details = DetailedReportService.buildSinkDetails(sink);

        assertThat(details).hasSize(3);
        assertBuildDetail(details.get(0), "0m~100m 사고 이력", "1건(최근 2023년 4월)", "지반침하 사고이력");
        assertBuildDetail(details.get(1), "100m~300m 사고 이력", "없음", "지반침하 사고이력");
        assertBuildDetail(details.get(2), "300m~500m 사고 이력", "2건(최근 2021년 8월)", "지반침하 사고이력");
    }

    @Test
    @DisplayName("침수 이력 없음은 이력 없음·해당 없음으로 표기한다")
    void buildFloodDetails_withoutHistoryMarksNoHistory() {
        FloodScoreResultDto flood = FloodScoreResultDto.of(100, List.of());

        List<ReportDetailDto> details = DetailedReportService.buildFloodDetails(flood);

        assertThat(details).hasSize(2);
        assertBuildDetail(details.get(0), "지번 침수 이력", "이력 없음", "행정안전부 침수흔적도");
        assertBuildDetail(details.get(1), "최고 침수심 등급", "해당 없음", "행정안전부 침수흔적도");
    }

    @Test
    @DisplayName("침수 이력이 있으면 건수·최근 연도와 최고 침수심 등급·해당 연도를 표기한다")
    void buildFloodDetails_withHistoryListsCountAndWorstGrade() {
        // 매퍼 정렬(연도 내림차순) 전제 — 첫 건이 최근 이력. 최고 등급(5)은 과거 건이라도 대표로 노출된다
        FloodScoreResultDto flood = FloodScoreResultDto.of(77, List.of(
                FloodIncidentDto.builder().year("2022").grade(3).sggCd("11380").cause("호우").build(),
                FloodIncidentDto.builder().year("2020").grade(5).sggCd("11380").cause("호우").build()));

        List<ReportDetailDto> details = DetailedReportService.buildFloodDetails(flood);

        assertBuildDetail(details.get(0), "지번 침수 이력", "2건(최근 2022년)", "행정안전부 침수흔적도");
        assertBuildDetail(details.get(1), "최고 침수심 등급", "5등급(2020년)", "행정안전부 침수흔적도");
    }

    private static void assertBuildDetail(ReportDetailDto detail, String label, String value, String source) {
        assertThat(detail.getLabel()).isEqualTo(label);
        assertThat(detail.getValue()).isEqualTo(value);
        assertThat(detail.getSource()).isEqualTo(source);
    }

    private static StructuralStabilityScoreResultDto structOf(StructuralFactorDto... factors) {
        return StructuralStabilityScoreResultDto.builder()
                .score(100)
                .factors(List.of(factors))
                .build();
    }

    private static StructuralFactorDto buildFactorOf(String factor, String detail) {
        return StructuralFactorDto.builder().factor(factor).detail(detail).deduction(0).build();
    }
}
