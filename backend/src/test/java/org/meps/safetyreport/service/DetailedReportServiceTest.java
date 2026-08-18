package org.meps.safetyreport.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.fire.dto.FireScoreInput;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.service.FireScoreService;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.service.FloodScoreService;
import org.meps.safetyreport.dto.BasicBriefingDto;
import org.meps.safetyreport.dto.BriefingInput;
import org.meps.safetyreport.dto.DetailedBriefingDto;
import org.meps.safetyreport.dto.DetailedFactorBaseDto;
import org.meps.safetyreport.dto.DetailedFactorDto;
import org.meps.safetyreport.dto.DetailedReportResponseDto;
import org.meps.safetyreport.dto.SafetyReportRowDto;
import org.meps.safetyreport.mapper.SafetyReportMapper;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.sinkhole.service.SinkholeScoreService;
import org.meps.structure.dto.StructuralFactorDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;
import org.meps.structure.service.StructuralStabilityScoreService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 상세 리포트 details 조립부 단위 테스트 */
class DetailedReportServiceTest {

    @Test
    @DisplayName("구조 details는 룰 엔진 감점 4요소를 건축물대장 출처로 나열한다")
    void buildStructDetails_listsRegisterBackedFactors() {
        StructuralStabilityScoreResultDto struct = structOf(
                buildFactorOf("USE_APR_DAY", "1978-12-29"),
                buildFactorOf("STRUCTURE_TYPE", "철근콘크리트구조"),
                buildFactorOf("VIOLATION", "Y"),
                buildFactorOf("UNDERGROUND_FLOOR", "지하 1층"));

        List<DetailedFactorBaseDto> details = DetailedReportService.buildStructDetails(struct);

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

        List<DetailedFactorBaseDto> details = DetailedReportService.buildStructDetails(struct);

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

        List<DetailedFactorBaseDto> details = DetailedReportService.buildFireDetails(fire);

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

        List<DetailedFactorBaseDto> details = DetailedReportService.buildFireDetails(fire);

        assertBuildDetail(details.get(0), "도로접면", "정보 없음", "토지특성정보");
        assertBuildDetail(details.get(1), "주구조", "정보 없음", "건축물대장 표제부");
        assertBuildDetail(details.get(2), "행정동 화재 건수(3년 평균)", "정보 없음", "소방청 화재통계");
        assertBuildDetail(details.get(3), "최근접 소방서 거리", "정보 없음", "소방서 위치정보");
    }

    @Test
    @DisplayName("지반침하 사고 없음은 세 거리 구간을 모두 없음으로 나열한다")
    void buildSinkDetails_withoutIncidentMarksAllBandsAsNone() {
        SinkholeScoreResult sink = SinkholeScoreResult.of(100, List.of());

        List<DetailedFactorBaseDto> details = DetailedReportService.buildSinkDetails(sink);

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

        List<DetailedFactorBaseDto> details = DetailedReportService.buildSinkDetails(sink);

        assertThat(details).hasSize(3);
        assertBuildDetail(details.get(0), "0m~100m 사고 이력", "1건(최근 2023년 4월)", "지반침하 사고이력");
        assertBuildDetail(details.get(1), "100m~300m 사고 이력", "없음", "지반침하 사고이력");
        assertBuildDetail(details.get(2), "300m~500m 사고 이력", "2건(최근 2021년 8월)", "지반침하 사고이력");
    }

    @Test
    @DisplayName("침수 이력 없음은 이력 없음·해당 없음으로 표기한다")
    void buildFloodDetails_withoutHistoryMarksNoHistory() {
        FloodScoreResultDto flood = FloodScoreResultDto.of(100, List.of());

        List<DetailedFactorBaseDto> details = DetailedReportService.buildFloodDetails(flood);

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

        List<DetailedFactorBaseDto> details = DetailedReportService.buildFloodDetails(flood);

        assertBuildDetail(details.get(0), "지번 침수 이력", "2건(최근 2022년)", "행정안전부 침수흔적도");
        assertBuildDetail(details.get(1), "최고 침수심 등급", "5등급(2020년)", "행정안전부 침수흔적도");
    }

    private static void assertBuildDetail(DetailedFactorBaseDto detail, String label, String value, String source) {
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


    /** getDetailedReport 테스트 */
    private static final String BUILDING_ID = "1121510500100120006000001";

    private static final DetailedBriefingDto CANNED_BRIEFING = DetailedBriefingDto.builder()
            .totalHeadline("종합 헤드라인")
            .totalSummary("종합 요약")
            .structReport("구조 해석")
            .fireReport("화재 해석")
            .sinkReport("지반침하 해석")
            .floodReport("침수 해석")
            .build();

    @Test
    @DisplayName("캐시 행에 report 5개 컬럼이 모두 있으면 LLM을 호출하지 않고 캐시된 값을 그대로 반환한다")
    void getDetailedReport_usesCachedReports_whenAllReportColumnsPresent() {
        FakeSafetyReportMapper mapper = new FakeSafetyReportMapper();
        mapper.row = SafetyReportRowDto.builder()
                .totalReport("캐시된 종합 리포트")
                .structReport("캐시된 구조 리포트")
                .fireReport("캐시된 화재 리포트")
                .sinkReport("캐시된 지반침하 리포트")
                .floodReport("캐시된 침수 리포트")
                .build();
        DetailedReportService service = serviceWith(generateShouldNotBeCalledBriefingService(), mapper);

        DetailedReportResponseDto response = service.getDetailedReport(BUILDING_ID);

        assertThat(response.getOverallAiReport()).isEqualTo("캐시된 종합 리포트");
        assertThat(factorByCode(response, "STRUCTURE").getAiReport()).isEqualTo("캐시된 구조 리포트");
        assertThat(factorByCode(response, "FIRE").getAiReport()).isEqualTo("캐시된 화재 리포트");
        assertThat(factorByCode(response, "SINKHOLE").getAiReport()).isEqualTo("캐시된 지반침하 리포트");
        assertThat(factorByCode(response, "FLOOD").getAiReport()).isEqualTo("캐시된 침수 리포트");
        assertThat(mapper.updateReportsCallCount).isZero();
    }

    @Test
    @DisplayName("캐시 행이 아예 없으면 LLM으로 새로 생성하고 결과를 updateReports로 저장한다")
    void getDetailedReport_generatesAndSaves_whenNoCachedRowExists() {
        FakeSafetyReportMapper mapper = new FakeSafetyReportMapper();
        mapper.row = null;
        DetailedReportService service = serviceWith(generatingBriefingService(CANNED_BRIEFING), mapper);

        DetailedReportResponseDto response = service.getDetailedReport(BUILDING_ID);

        assertThat(response.getOverallAiReport()).isEqualTo("종합 헤드라인\n\n종합 요약");
        assertThat(factorByCode(response, "STRUCTURE").getAiReport()).isEqualTo("구조 해석");
        assertThat(factorByCode(response, "FIRE").getAiReport()).isEqualTo("화재 해석");
        assertThat(factorByCode(response, "SINKHOLE").getAiReport()).isEqualTo("지반침하 해석");
        assertThat(factorByCode(response, "FLOOD").getAiReport()).isEqualTo("침수 해석");

        assertThat(mapper.updateReportsCallCount).isEqualTo(1);
        assertThat(mapper.savedTotalReport).isEqualTo("종합 헤드라인\n\n종합 요약");
        assertThat(mapper.savedStructReport).isEqualTo("구조 해석");
        assertThat(mapper.savedFireReport).isEqualTo("화재 해석");
        assertThat(mapper.savedSinkReport).isEqualTo("지반침하 해석");
        assertThat(mapper.savedFloodReport).isEqualTo("침수 해석");
    }

    @Test
    @DisplayName("점수 행은 있지만 report 컬럼 일부가 비어 있으면 다시 생성해서 저장한다")
    void getDetailedReport_regenerates_whenRowExistsButReportsPartiallyMissing() {
        FakeSafetyReportMapper mapper = new FakeSafetyReportMapper();
        mapper.row = SafetyReportRowDto.builder()
                .totalReport("캐시된 종합 리포트")
                .structReport(null) // 구조만 비어 있음 — hasAllReports가 false를 반환해야 한다
                .fireReport("캐시된 화재 리포트")
                .sinkReport("캐시된 지반침하 리포트")
                .floodReport("캐시된 침수 리포트")
                .build();
        DetailedReportService service = serviceWith(generatingBriefingService(CANNED_BRIEFING), mapper);

        service.getDetailedReport(BUILDING_ID);

        assertThat(mapper.updateReportsCallCount).isEqualTo(1);
        assertThat(mapper.savedStructReport).isEqualTo("구조 해석");
    }

    @Test
    @DisplayName("LLM 호출이 실패하면 폴백 응답을 반환하고 DB에는 저장하지 않는다")
    void getDetailedReport_fallsBackWithoutSaving_whenLlmGenerationFails() {
        FakeSafetyReportMapper mapper = new FakeSafetyReportMapper();
        mapper.row = null;
        DetailedReportService service = serviceWith(failingBriefingService(), mapper);

        DetailedReportResponseDto response = service.getDetailedReport(BUILDING_ID);

        // 폴백 텍스트는 등급 기반 고정 템플릿 — 스텁 점수(구조92/화재97/지반침하100/침수100)는 모두 안전 등급
        assertThat(response.getOverallAiReport())
                .isEqualTo("주요 진단 항목에서 특이 이력이 확인되지 않은 건물이에요.\n\n종합 등급은 안전이에요."
                        + " 4개 항목의 세부 근거와 해석은 아래 항목별 상세 진단에서 확인해 보세요.");
        assertThat(factorByCode(response, "STRUCTURE").getAiReport())
                .isEqualTo("구조 항목은 세부 근거에서 특이 이력이 확인되지 않아 안전 등급으로 판정됐어요. "
                        + "현재까지 확인된 위험 요인은 없어요. "
                        + "별도 조치 없이 위 상세 근거 항목을 참고만 하셔도 좋아요.");
        assertThat(mapper.updateReportsCallCount).isZero();
    }

    @Test
    @DisplayName("같은 건물의 생성이 진행 중이면 중복 요청은 스레드 풀에 넘기지 않는다")
    void requestDetailedReportAsync_skipsSubmission_whenSameBuildingInFlight() {
        FakeSafetyReportMapper mapper = new FakeSafetyReportMapper();
        DetailedReportService service = serviceWith(generateShouldNotBeCalledBriefingService(), mapper);
        int[] submitCount = {0};
        // 호출 횟수만 세고 완료 처리(표식 제거)는 하지 않는 스텁 — 첫 작업이 아직 실행 중인 상황 재현
        service.self = submitCountingStub(submitCount);

        service.requestDetailedReportAsync(BUILDING_ID);
        service.requestDetailedReportAsync(BUILDING_ID);

        assertThat(submitCount[0]).isEqualTo(1);
    }

    @Test
    @DisplayName("생성이 완료된 뒤의 요청은 다시 스레드 풀에 넘겨진다")
    void requestDetailedReportAsync_resubmits_afterPreviousGenerationCompleted() {
        FakeSafetyReportMapper mapper = new FakeSafetyReportMapper();
        DetailedReportService service = serviceWith(generateShouldNotBeCalledBriefingService(), mapper);
        int[] submitCount = {0};
        // 넘겨받은 즉시 완료되는 스텁 — 실제 비동기 작업의 finally(표식 제거)까지 재현
        service.self = new DetailedReportService(null, null, null, null, null, null, null) {
            @Override
            public void generateDetailedReportAsync(String buildingId) {
                submitCount[0]++;
                service.inFlightBuildings.remove(buildingId);
            }
        };

        service.requestDetailedReportAsync(BUILDING_ID);
        service.requestDetailedReportAsync(BUILDING_ID);

        assertThat(submitCount[0]).isEqualTo(2);
    }

    @Test
    @DisplayName("스레드 풀이 작업을 거부하면 진행 중 표식을 지워 다음 요청이 재시도할 수 있다")
    void requestDetailedReportAsync_clearsInFlightMark_whenSubmissionFails() {
        FakeSafetyReportMapper mapper = new FakeSafetyReportMapper();
        DetailedReportService service = serviceWith(generateShouldNotBeCalledBriefingService(), mapper);
        service.self = new DetailedReportService(null, null, null, null, null, null, null) {
            @Override
            public void generateDetailedReportAsync(String buildingId) {
                throw new java.util.concurrent.RejectedExecutionException("큐 포화(테스트용)");
            }
        };

        assertThatThrownBy(() -> service.requestDetailedReportAsync(BUILDING_ID))
                .isInstanceOf(java.util.concurrent.RejectedExecutionException.class);

        assertThat(service.inFlightBuildings).isEmpty();
    }

    @Test
    @DisplayName("비동기 생성 작업이 끝나면 진행 중 표식이 지워진다")
    void generateDetailedReportAsync_clearsInFlightMark_afterCompletion() {
        FakeSafetyReportMapper mapper = new FakeSafetyReportMapper();
        mapper.row = SafetyReportRowDto.builder()
                .totalReport("캐시된 종합 리포트")
                .structReport("캐시된 구조 리포트")
                .fireReport("캐시된 화재 리포트")
                .sinkReport("캐시된 지반침하 리포트")
                .floodReport("캐시된 침수 리포트")
                .build();
        DetailedReportService service = serviceWith(generateShouldNotBeCalledBriefingService(), mapper);
        service.inFlightBuildings.add(BUILDING_ID);

        service.generateDetailedReportAsync(BUILDING_ID);

        assertThat(service.inFlightBuildings).isEmpty();
    }

    private static DetailedReportService submitCountingStub(int[] submitCount) {
        return new DetailedReportService(null, null, null, null, null, null, null) {
            @Override
            public void generateDetailedReportAsync(String buildingId) {
                submitCount[0]++;
            }
        };
    }

    private static DetailedFactorDto factorByCode(DetailedReportResponseDto response, String code) {
        return response.getFactors().stream()
                .filter(f -> f.getCode().equals(code))
                .findFirst()
                .orElseThrow();
    }

    private static DetailedReportService serviceWith(DetailedBriefingService detailedBriefingService, FakeSafetyReportMapper mapper) {
        return new DetailedReportService(
                fireScoreServiceStub(FireScoreResult.of(97, FireScoreInput.builder().build(), null)),
                sinkholeScoreServiceStub(SinkholeScoreResult.of(100, List.of())),
                structuralStabilityScoreServiceStub(
                        StructuralStabilityScoreResultDto.of(92, List.of(buildFactorOf("STRUCTURE_TYPE", "철근콘크리트구조")))),
                floodScoreServiceStub(FloodScoreResultDto.of(100, List.of())),
                new TotalScoreService(),
                detailedBriefingService,
                mapper);
    }

    private static FireScoreService fireScoreServiceStub(FireScoreResult result) {
        return new FireScoreService(null) {
            @Override
            public FireScoreResult getFireScore(String buildingId) {
                return result;
            }
        };
    }

    private static SinkholeScoreService sinkholeScoreServiceStub(SinkholeScoreResult result) {
        return new SinkholeScoreService(null) {
            @Override
            public SinkholeScoreResult getSinkholeScore(String buildingId) {
                return result;
            }
        };
    }

    private static StructuralStabilityScoreService structuralStabilityScoreServiceStub(StructuralStabilityScoreResultDto result) {
        return new StructuralStabilityScoreService(null) {
            @Override
            public StructuralStabilityScoreResultDto getStructuralStabilityScore(String buildingId) {
                return result;
            }
        };
    }

    private static FloodScoreService floodScoreServiceStub(FloodScoreResultDto result) {
        return new FloodScoreService(null) {
            @Override
            public FloodScoreResultDto getFloodScore(String buildingId) {
                return result;
            }
        };
    }

    /** generate()가 호출되면 테스트가 실패하도록 만드는 스텁 — 캐시 hit 경로에서 LLM 미호출을 검증 */
    private static DetailedBriefingService generateShouldNotBeCalledBriefingService() {
        return new DetailedBriefingService(null, null, null) {
            @Override
            public DetailedBriefingDto generate(BriefingInput input) {
                throw new AssertionError("캐시 hit 상황에서는 generate()가 호출되면 안 된다");
            }
        };
    }

    private static DetailedBriefingService generatingBriefingService(DetailedBriefingDto generated) {
        return new DetailedBriefingService(null, null, null) {
            @Override
            public DetailedBriefingDto generate(BriefingInput input) {
                return generated;
            }
        };
    }

    /** fallback()은 오버라이드하지 않는다 — 실제 등급별 템플릿 로직 자체도 함께 검증하기 위해 */
    private static DetailedBriefingService failingBriefingService() {
        return new DetailedBriefingService(null, null, null) {
            @Override
            public DetailedBriefingDto generate(BriefingInput input) {
                throw new LlmCallFailedException("LLM 호출 실패(테스트용)");
            }
        };
    }

    /** SafetyReportMapper 인메모리 대역 — DetailedReportService는 findByBdMgtSn/updateReports만 사용한다 */
    private static class FakeSafetyReportMapper implements SafetyReportMapper {
        private SafetyReportRowDto row;
        private int updateReportsCallCount;
        private String savedTotalReport;
        private String savedFloodReport;
        private String savedSinkReport;
        private String savedFireReport;
        private String savedStructReport;

        @Override
        public SafetyReportRowDto findByBdMgtSn(String bdMgtSn) {
            return row;
        }

        @Override
        public void upsertScores(String bdMgtSn, String aiModelNm, int totalScore, int floodScore,
                                  int sinkScore, int fireScore, int structScore) {
            throw new UnsupportedOperationException("DetailedReportService는 upsertScores를 호출하지 않는다");
        }

        @Override
        public void updateBriefs(String bdMgtSn, String aiModelNm, BasicBriefingDto briefs) {
            throw new UnsupportedOperationException("DetailedReportService는 updateBriefs를 호출하지 않는다");
        }

        @Override
        public int tryClaimBriefGeneration(String bdMgtSn) {
            throw new UnsupportedOperationException("DetailedReportService는 tryClaimBriefGeneration을 호출하지 않는다");
        }

        @Override
        public void releaseBriefClaim(String bdMgtSn) {
            throw new UnsupportedOperationException("DetailedReportService는 releaseBriefClaim을 호출하지 않는다");
        }

        @Override
        public void updateReports(String bdMgtSn, String totalReport, String floodReport,
                                   String sinkReport, String fireReport, String structReport) {
            updateReportsCallCount++;
            savedTotalReport = totalReport;
            savedFloodReport = floodReport;
            savedSinkReport = sinkReport;
            savedFireReport = fireReport;
            savedStructReport = structReport;
        }
    }
}
