package org.meps.report.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.config.ServletConfig;
import org.meps.report.dto.SafetyReportRowDto;
import org.meps.report.mapper.SafetyReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 실 DB(3307 터널) + 실 OpenAI 호출 통합 테스트 — 터널·API 키가 있어야 성공한다 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(classes = RootConfig.class),
        @ContextConfiguration(classes = ServletConfig.class)
})
class SafetyReportControllerIntegrationTest {

    // 반경 500m 내 지반침하 사고 이력 1건 보유 건물 — 팩터별 사실이 프롬프트에 실리는 경로 검증
    private static final String BUILDING_ID = "1121510500100120006000001";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SafetyReportMapper safetyReportMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void 기본_리포트는_브리핑을_생성해_캐시하고_재호출시_동일_응답을_반환한다() throws Exception {
        String first = callBasicReport();
        JsonNode root = new ObjectMapper().readTree(first);

        assertThat(root.get("safetyScore").asInt()).isBetween(70, 100);
        assertThat(root.get("overallStatus").asText()).isIn("CAUTION", "GOOD", "SAFE");
        assertThat(root.get("overallBriefing").asText()).isNotBlank();

        JsonNode factors = root.get("factors");
        assertThat(factors.size()).isEqualTo(4);
        assertThat(factors.get(0).get("code").asText()).isEqualTo("STRUCTURE");
        assertThat(factors.get(1).get("code").asText()).isEqualTo("FIRE");
        assertThat(factors.get(2).get("code").asText()).isEqualTo("SINKHOLE");
        assertThat(factors.get(3).get("code").asText()).isEqualTo("FLOOD");
        for (JsonNode factor : factors) {
            assertThat(factor.get("status").asText()).isIn("CAUTION", "GOOD", "SAFE");
            assertThat(factor.get("briefing").asText()).isNotBlank();
        }

        // 폴백이었다면 brief는 NULL로 남는다 — 캐시 저장 여부가 실제 LLM 생성 성공의 증거
        SafetyReportRowDto row = safetyReportMapper.findByBdMgtSn(BUILDING_ID);
        assertThat(row).isNotNull();
        assertThat(row.getTotalBrief()).isNotBlank();
        assertThat(row.getStructBrief()).isNotBlank();
        assertThat(row.getFireBrief()).isNotBlank();
        assertThat(row.getSinkBrief()).isNotBlank();
        assertThat(row.getFloodBrief()).isNotBlank();

        // 점수 불변 + brief 존재 → 캐시 hit, LLM 재호출 없이 동일 문장
        String second = callBasicReport();
        assertThat(second).isEqualTo(first);
    }

    private String callBasicReport() throws Exception {
        return mockMvc.perform(get("/api/buildings/{buildingId}/safety-report/basic", BUILDING_ID))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
