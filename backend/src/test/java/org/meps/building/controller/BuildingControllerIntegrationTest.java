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
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "37.4913962").param("swLng", "127.0268891")
                        .param("neLat", "37.5098784").param("neLng", "127.0464593")
                        .param("zoom", "17"))
                .andExpect(status().isOk());
    }

    @Test
    void 줌이_부족해도_200을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "37.4913962").param("swLng", "127.0268891")
                        .param("neLat", "37.5098784").param("neLng", "127.0464593")
                        .param("zoom", "16"))
                .andExpect(status().isOk());
    }

    @Test
    void 좌표_범위_오류는_400을_반환한다() throws Exception {
        // sw와 ne를 뒤바꾼 요청
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "37.5098784").param("swLng", "127.0464593")
                        .param("neLat", "37.4913962").param("neLng", "127.0268891")
                        .param("zoom", "17"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 필수_파라미터가_누락되면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "37.4913962"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 파라미터_타입이_잘못되면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/nearby")
                        .param("swLat", "abc").param("swLng", "127.0268891")
                        .param("neLat", "37.5098784").param("neLng", "127.0464593")
                        .param("zoom", "17"))
                .andExpect(status().isBadRequest());
    }
}
