package org.meps.hjd.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.config.ServletConfig;
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

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(classes = RootConfig.class),
        @ContextConfiguration(classes = ServletConfig.class)
})
class HjdControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void 행정동_리스트_조회는_200과_425건을_반환한다() throws Exception {
        String body = mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode regions = new ObjectMapper().readTree(body).get("regions");

        assertThat(regions.size()).isEqualTo(425);
    }

    @Test
    void 구별_AI_브리핑_조회는_200과_통계_브리핑을_반환한다() throws Exception {
        // 11110 종로구
        String body = mockMvc.perform(get("/api/ssg/{sggCd}/briefing", "11110"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode root = new ObjectMapper().readTree(body);

        assertThat(root.get("sggName").asText()).isNotBlank();
        assertThat(root.get("dailyFlpop").asInt()).isPositive();
        assertThat(root.get("overallBriefing").asText()).isNotBlank();
    }

    @Test
    void 존재하지_않는_구_코드로_조회하면_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/ssg/{sggCd}/briefing", "99999"))
                .andExpect(status().isNotFound());
    }

}
