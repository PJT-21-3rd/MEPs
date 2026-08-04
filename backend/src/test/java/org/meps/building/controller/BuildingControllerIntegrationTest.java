package org.meps.building.controller;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(classes = RootConfig.class),
        @ContextConfiguration(classes = ServletConfig.class)
})
class BuildingControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void 정상_요청은_200을_반환한다() throws Exception {
        // 광진구 자양동·구의동 일대 (데이터 존재 영역)
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "37.5250").param("swLng", "127.0550")
                        .param("neLat", "37.5450").param("neLng", "127.1000")
                        .param("zoom", "17"))
                .andExpect(status().isOk());
    }

    @Test
    void 줌이_부족해도_200을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "37.5250").param("swLng", "127.0550")
                        .param("neLat", "37.5450").param("neLng", "127.1000")
                        .param("zoom", "16"))
                .andExpect(status().isOk());
    }

    @Test
    void 좌표_범위_오류는_400을_반환한다() throws Exception {
        // sw와 ne를 뒤바꾼 요청
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "37.5450").param("swLng", "127.1000")
                        .param("neLat", "37.5250").param("neLng", "127.0550")
                        .param("zoom", "17"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 필수_파라미터가_누락되면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "37.5250"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 파라미터_타입이_잘못되면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "abc").param("swLng", "127.0550")
                        .param("neLat", "37.5450").param("neLng", "127.1000")
                        .param("zoom", "17"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 상세조회_정상_요청은_200을_반환한다() throws Exception {
        // 동인빌딩 (광진구 중곡동)
        mockMvc.perform(get("/api/buildings/{buildingId}", "1121510100100180054000039"))
                .andExpect(status().isOk());
    }

    @Test
    void 상세조회_도로명주소가_없는_건물도_200을_반환한다() throws Exception {
        // 아차산관리사무소 — 건축물대장에 도로명주소 미등재 (결측 필드는 null)
        mockMvc.perform(get("/api/buildings/{buildingId}", "1121510100100030059005620"))
                .andExpect(status().isOk());
    }

    @Test
    void 상세조회_buildingId_형식이_잘못되면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/{buildingId}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 상세조회_존재하지_않는_buildingId는_404를_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/{buildingId}", "9999999999999999999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 상세조회_응답에_건물폴리곤이_포함된다() throws Exception {
        String body = mockMvc.perform(get("/api/buildings/{buildingId}", "1121510100100030059005620"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertTrue(body.contains("\"footprint\""));
        assertTrue(body.contains("MultiPolygon"));
    }
}
