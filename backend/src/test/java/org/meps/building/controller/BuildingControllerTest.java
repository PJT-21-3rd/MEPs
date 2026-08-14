package org.meps.building.controller;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class})
@Transactional
@DisplayName("건물 조회 찜 여부 통합 테스트")
class BuildingControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserMapper userMapper;

    private MockMvc mockMvc;
    private String token;

    private static final String BUILDING_ID = "1121510100100030059005620";
    private static final double LAT = 37.562335;
    private static final double LNG = 127.0963272;

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

    /** 응답 본문에서 saved 값을 확인한다 (jsonPath 대신 문자열 검사) */
    private void assertSaved(MvcResult result, boolean expected) throws Exception {
        assertThat(result.getResponse().getContentAsString())
                .contains("\"saved\":" + expected);
    }

    // ---------- 상세 조회 ----------

    @Test
    @DisplayName("비로그인 상세 조회 → saved false")
    void detail_anonymous() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/buildings/{id}", BUILDING_ID))
                .andExpect(status().isOk())
                .andReturn();

        assertSaved(result, false);
    }

    @Test
    @DisplayName("찜하지 않은 건물 상세 조회 → saved false")
    void detail_loggedInNotSaved() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/buildings/{id}", BUILDING_ID)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn();

        assertSaved(result, false);
    }

    @Test
    @DisplayName("찜한 건물 상세 조회 → saved true")
    void detail_loggedInSaved() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));

        MvcResult result = mockMvc.perform(get("/api/buildings/{id}", BUILDING_ID)
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn();

        assertSaved(result, true);
    }

    // ---------- 좌표 조회 ----------

    @Test
    @DisplayName("비로그인 좌표 조회 → saved false")
    void detailAt_anonymous() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/buildings/point")
                        .param("lat", String.valueOf(LAT))
                        .param("lng", String.valueOf(LNG)))
                .andExpect(status().isOk())
                .andReturn();

        assertSaved(result, false);
    }

    @Test
    @DisplayName("찜한 건물 좌표 조회 → saved true")
    void detailAt_loggedInSaved() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));

        MvcResult result = mockMvc.perform(get("/api/buildings/point")
                        .param("lat", String.valueOf(LAT))
                        .param("lng", String.valueOf(LNG))
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn();

        assertSaved(result, true);
    }
}