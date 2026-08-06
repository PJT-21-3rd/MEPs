package org.meps.sinkhole.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.common.util.SafetyGrade;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class SinkholeScoreServiceIntegrationTest {

    // 성수동2가 사고(20260048) 반경 500m에 매핑된 자양동 건물
    private static final String MAPPED_BUILDING = "1121510500100120006000001";
    // 매핑이 없는 광진구 건물
    private static final String UNMAPPED_BUILDING = "1121510700100010000011337";

    @Autowired
    private SinkholeScoreService sinkholeScoreService;

    @Test
    void 사고_이력이_매핑된_건물은_이력_플래그와_사고_목록을_반환한다() {
        SinkholeScoreResult result = sinkholeScoreService.getSinkholeScore(MAPPED_BUILDING);

        assertThat(result.getScore()).isBetween(70, 100);
        assertThat(result.isSubsidenceHistory()).isTrue();
        assertThat(result.getIncidentCount()).isEqualTo(result.getIncidents().size());
        for (SinkholeIncidentDto incident : result.getIncidents()) {
            assertThat(incident.getDistanceM()).isLessThanOrEqualTo(500.0);
            assertThat(incident.getSagoDate()).hasSize(8);
        }
    }

    @Test
    void 사고_이력이_없는_건물은_100점_안전이다() {
        SinkholeScoreResult result = sinkholeScoreService.getSinkholeScore(UNMAPPED_BUILDING);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.isSubsidenceHistory()).isFalse();
        assertThat(result.getIncidents()).isEmpty();
    }
}
