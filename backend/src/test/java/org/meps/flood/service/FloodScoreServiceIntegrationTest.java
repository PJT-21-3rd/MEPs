package org.meps.flood.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.common.util.SafetyGrade;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class FloodScoreServiceIntegrationTest {

    // building_flood_map에 매핑된 건물
    private static final String MAPPED_BUILDING = "1168010800101610015008726";
    // 매핑이 없는 건물
    private static final String UNMAPPED_BUILDING = "1121510500107670000014427";

    @Autowired
    private FloodScoreService floodScoreService;

    @Test
    void 침수_이력이_매핑된_건물은_이력_플래그와_이력_목록을_반환한다() {
        FloodScoreResultDto result = floodScoreService.getFloodScore(MAPPED_BUILDING);

        System.out.println(result); // score, grade, incidentCount 등 필드 전체 출력
        result.getIncidents().forEach(System.out::println); // year/grade/cause 등 이력 상세

        assertThat(result.getScore()).isBetween(70, 100);
        assertThat(result.isFloodHistory()).isTrue();
        assertThat(result.getIncidentCount()).isEqualTo(result.getIncidents().size());
        for (FloodIncidentDto incident : result.getIncidents()) {
            assertThat(incident.getYear()).hasSize(4);
        }
    }

    @Test
    void 침수_이력이_없는_건물은_100점_안전이다() {
        FloodScoreResultDto result = floodScoreService.getFloodScore(UNMAPPED_BUILDING);

        System.out.println(result); // score, grade, incidentCount 등 필드 전체 출력
        result.getIncidents().forEach(System.out::println); // year/grade/cause 등 이력 상세

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.isFloodHistory()).isFalse();
        assertThat(result.getIncidents()).isEmpty();
    }
}
