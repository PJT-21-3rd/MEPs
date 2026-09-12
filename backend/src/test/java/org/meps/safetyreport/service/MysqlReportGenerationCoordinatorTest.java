package org.meps.safetyreport.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.safetyreport.dto.BasicBriefingDto;
import org.meps.safetyreport.dto.SafetyReportRowDto;
import org.meps.safetyreport.mapper.SafetyReportMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** MySQL 상태 컬럼 코디네이터 — 클레임 판정과 폴링 대기 검증 */
class MysqlReportGenerationCoordinatorTest {

    private static final String BUILDING_ID = "1111111111111111111111111";

    @Test
    @DisplayName("클레임 UPDATE의 affected rows가 1이면 리더다")
    void claimSucceedsWhenUpdateAffectsOneRow() {
        FakeMapper mapper = new FakeMapper();
        mapper.claimAffectedRows = 1;
        MysqlReportGenerationCoordinator coordinator = new MysqlReportGenerationCoordinator(mapper, 1L);

        assertThat(coordinator.tryClaim(BUILDING_ID)).isTrue();
    }

    @Test
    @DisplayName("클레임 UPDATE의 affected rows가 0이면 탈락이다")
    void claimFailsWhenUpdateAffectsNoRow() {
        FakeMapper mapper = new FakeMapper();
        mapper.claimAffectedRows = 0;
        MysqlReportGenerationCoordinator coordinator = new MysqlReportGenerationCoordinator(mapper, 1L);

        assertThat(coordinator.tryClaim(BUILDING_ID)).isFalse();
    }

    @Test
    @DisplayName("브리핑이 이미 채워져 있으면 폴링 첫 조회에서 즉시 반환한다")
    void awaitReturnsImmediatelyWhenBriefsAlreadyPresent() {
        FakeMapper mapper = new FakeMapper();
        mapper.row = rowWithAllBriefs();
        MysqlReportGenerationCoordinator coordinator = new MysqlReportGenerationCoordinator(mapper, 1L);

        BasicBriefingDto result = coordinator.awaitResult(BUILDING_ID);

        assertThat(result).isNotNull();
        assertThat(result.getTotalBrief()).isEqualTo("종합");
        assertThat(mapper.findCallCount).isEqualTo(1);
    }

    @Test
    @DisplayName("폴링 도중 브리핑이 채워지면 그 시점에 반환한다")
    void awaitReturnsWhenBriefsAppearMidPolling() {
        FakeMapper mapper = new FakeMapper();
        mapper.rowAvailableFromCall = 3;
        mapper.row = rowWithAllBriefs();
        MysqlReportGenerationCoordinator coordinator = new MysqlReportGenerationCoordinator(mapper, 1L);

        BasicBriefingDto result = coordinator.awaitResult(BUILDING_ID);

        assertThat(result).isNotNull();
        assertThat(mapper.findCallCount).isEqualTo(3);
    }

    @Test
    @DisplayName("폴링 한도까지 브리핑이 안 채워지면 null을 반환한다")
    void awaitReturnsNullWhenBriefsNeverAppear() {
        FakeMapper mapper = new FakeMapper();
        MysqlReportGenerationCoordinator coordinator = new MysqlReportGenerationCoordinator(mapper, 1L);

        BasicBriefingDto result = coordinator.awaitResult(BUILDING_ID);

        assertThat(result).isNull();
        assertThat(mapper.findCallCount).isEqualTo(MysqlReportGenerationCoordinator.POLL_MAX_ATTEMPTS);
    }

    @Test
    @DisplayName("complete는 mapper.updateBriefs를 source=LLM으로 호출한다")
    void completeCallsUpdateBriefsWithLlmSource() {
        FakeMapper mapper = new FakeMapper();
        MysqlReportGenerationCoordinator coordinator = new MysqlReportGenerationCoordinator(mapper, 1L);
        BasicBriefingDto briefs = BasicBriefingDto.builder().totalBrief("종합").build();

        coordinator.complete(BUILDING_ID, "model-a", briefs);

        assertThat(mapper.lastUpdateSource).isEqualTo("LLM");
        assertThat(mapper.lastUpdateBriefs).isSameAs(briefs);
    }

    @Test
    @DisplayName("completeFallback은 mapper.updateBriefs를 source=FALLBACK으로 호출한다")
    void completeFallbackCallsUpdateBriefsWithFallbackSource() {
        FakeMapper mapper = new FakeMapper();
        MysqlReportGenerationCoordinator coordinator = new MysqlReportGenerationCoordinator(mapper, 1L);
        BasicBriefingDto briefs = BasicBriefingDto.builder().totalBrief("폴백").build();

        coordinator.completeFallback(BUILDING_ID, "model-a", briefs);

        assertThat(mapper.lastUpdateSource).isEqualTo("FALLBACK");
        assertThat(mapper.lastUpdateBriefs).isSameAs(briefs);
    }

    private static SafetyReportRowDto rowWithAllBriefs() {
        return SafetyReportRowDto.builder()
                .bdMgtSn(BUILDING_ID)
                .totalBrief("종합")
                .structBrief("구조")
                .fireBrief("화재")
                .sinkBrief("지반침하")
                .floodBrief("침수")
                .build();
    }

    /** SafetyReportMapper 대역 — 코디네이터는 클레임·해제·저장·조회 4개만 사용한다 */
    private static class FakeMapper implements SafetyReportMapper {
        private int claimAffectedRows;
        private SafetyReportRowDto row;
        private int rowAvailableFromCall = 1; // 이 회차 조회부터 row를 돌려준다
        private int findCallCount;
        private String lastUpdateSource;
        private BasicBriefingDto lastUpdateBriefs;

        @Override
        public SafetyReportRowDto findByBdMgtSn(String bdMgtSn) {
            findCallCount++;
            if (findCallCount < rowAvailableFromCall) {
                return null;
            }
            return row;
        }

        @Override
        public int tryClaimBriefGeneration(String bdMgtSn) {
            return claimAffectedRows;
        }

        @Override
        public void updateBriefs(String bdMgtSn, String aiModelNm, BasicBriefingDto briefs, String source) {
            lastUpdateSource = source;
            lastUpdateBriefs = briefs;
        }

        @Override
        public void upsertScores(String bdMgtSn, String aiModelNm, int totalScore, int floodScore,
                                  int sinkScore, int fireScore, int structScore) {
            throw new UnsupportedOperationException("코디네이터는 upsertScores를 호출하지 않는다");
        }

        @Override
        public void updateReports(String bdMgtSn, String totalReport, String floodReport,
                                   String sinkReport, String fireReport, String structReport) {
            throw new UnsupportedOperationException("코디네이터는 updateReports를 호출하지 않는다");
        }
    }
}
