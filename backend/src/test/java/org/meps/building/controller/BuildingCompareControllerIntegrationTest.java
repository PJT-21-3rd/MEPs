package org.meps.building.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.config.ServletConfig;
import org.meps.user.dto.UserDto;
import org.meps.user.jwt.JwtProvider;
import org.meps.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(classes = RootConfig.class),
        @ContextConfiguration(classes = ServletConfig.class)
})
@Transactional
@DisplayName("매물 비교 조회 API 통합 테스트")
class BuildingCompareControllerIntegrationTest {

    // 기존 BuildingControllerIntegrationTest에서도 쓰는, 실 DB에 존재하는 건물관리번호 3건
    private static final String BUILDING_A = "1121510100100180054000039";
    private static final String BUILDING_B = "1121510100100030059005620";
    private static final String BUILDING_C = "1168010100106010003000001";

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private UserMapper userMapper;

    private MockMvc mockMvc;
    private String token;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        UserDto user = UserDto.builder()
                .email("test-" + System.nanoTime() + "@meps.local")
                .password("encoded")
                .build();
        userMapper.insertUser(user);
        token = jwtProvider.createToken(user.getUserId());
    }

    private String bearer() {
        return "Bearer " + token;
    }

    private void save(String buildingId) throws Exception {
        mockMvc.perform(post("/api/member/saved/{buildingId}", buildingId)
                .header("Authorization", bearer()));
    }


    @Test
    @DisplayName("찜한 매물 2개 비교 성공 시 200을 반환한다")
    void compare_success_returns200() throws Exception {
        save(BUILDING_A);
        save(BUILDING_B);

        mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, BUILDING_B)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("찜한 매물 3개 비교 성공 시 200을 반환한다")
    void compare_withThreeBuildings_returns200() throws Exception {
        save(BUILDING_A);
        save(BUILDING_B);
        save(BUILDING_C);

        String body = mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, BUILDING_B, BUILDING_C)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(objectMapper.readTree(body).get("buildings")).hasSize(3);
    }

    @Test
    @DisplayName("응답의 각 항목에 building과 safetyReport가 함께 포함된다")
    void compare_responseContainsBuildingAndSafetyReportPerItem() throws Exception {
        save(BUILDING_A);
        save(BUILDING_B);

        String body = mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, BUILDING_B)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode buildings = objectMapper.readTree(body).get("buildings");
        assertThat(buildings).hasSize(2);
        for (JsonNode item : buildings) {
            assertThat(item.get("building").get("buildingId").asText()).isNotBlank();
            assertThat(item.get("building").has("land")).isTrue();
            assertThat(item.get("building").has("detail")).isTrue();

            JsonNode safetyReport = item.get("safetyReport");
            assertThat(safetyReport.get("safetyScore").asInt()).isBetween(0, 100);
            assertThat(safetyReport.get("overallStatus").asText()).isIn("CAUTION", "GOOD", "SAFE");
            assertThat(safetyReport.get("factors")).hasSize(4);
        }
    }

    @Test
    @DisplayName("응답 순서는 요청한 buildingIds 순서와 동일하다")
    void compare_responseOrderMatchesRequestOrder() throws Exception {
        save(BUILDING_A);
        save(BUILDING_B);

        String body = mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_B, BUILDING_A)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        JsonNode buildings = objectMapper.readTree(body).get("buildings");
        assertThat(buildings.get(0).get("building").get("buildingId").asText()).isEqualTo(BUILDING_B);
        assertThat(buildings.get(1).get("building").get("buildingId").asText()).isEqualTo(BUILDING_A);
    }

    // ---------- 개수/중복 검증 (400) ----------
    @Test
    @DisplayName("비교 매물이 1개면 400을 반환한다")
    void compare_withOneBuildingId_returns400() throws Exception {
        mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A)
                        .header("Authorization", bearer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비교 매물이 4개면 400을 반환한다")
    void compare_withFourBuildingIds_returns400() throws Exception {
        mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, BUILDING_B, BUILDING_C, BUILDING_A)
                        .header("Authorization", bearer()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복된 buildingId가 있으면 400을 반환한다")
    void compare_withDuplicateBuildingIds_returns400() throws Exception {
        // 개수/중복 검증은 찜 여부 검증보다 먼저 실행되므로 찜하지 않아도 400으로 걸러진다
        mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, BUILDING_A)
                        .header("Authorization", bearer()))
                .andExpect(status().isBadRequest());
    }

    // ---------- 찜 검증 (403) ----------
    @Test
    @DisplayName("찜하지 않은 매물이 섞여 있으면 403을 반환한다")
    void compare_withUnsavedBuilding_returns403() throws Exception {
        save(BUILDING_A);
        // BUILDING_B는 찜하지 않음

        mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, BUILDING_B)
                        .header("Authorization", bearer()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("아무것도 찜하지 않은 상태로 요청하면 403을 반환한다")
    void compare_withNoSavedBuildings_returns403() throws Exception {
        mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, BUILDING_B)
                        .header("Authorization", bearer()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("형식이 잘못된 buildingId도 찜 목록에 없으므로 403을 반환한다")
    void compare_withMalformedBuildingId_returns403() throws Exception {
        save(BUILDING_A);

        mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, "abc")
                        .header("Authorization", bearer()))
                .andExpect(status().isForbidden());
    }

    // ---------- 인증 (401) ----------
    @Test
    @DisplayName("토큰 없이 요청하면 401을 반환한다")
    void compare_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/buildings/compare")
                        .param("buildingIds", BUILDING_A, BUILDING_B))
                .andExpect(status().isUnauthorized());
    }
}
