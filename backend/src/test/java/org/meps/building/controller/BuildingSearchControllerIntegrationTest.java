package org.meps.building.controller;

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

/**
 * 통합 검색 통합 테스트 - 실제 DB(SSH 터널) + V-World API 필요
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(classes = RootConfig.class),
        @ContextConfiguration(classes = ServletConfig.class)
})
class BuildingSearchControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private JsonNode search(String keyword) throws Exception {
        String body = mockMvc.perform(get("/api/buildings/search").param("keyword", keyword))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    @Test
    void 키워드가_누락되면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 키워드가_공백이면_400을_반환한다() throws Exception {
        mockMvc.perform(get("/api/buildings/search").param("keyword", "   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 행정동명_검색은_REGION과_bbox를_반환한다() throws Exception {
        JsonNode json = search("화양동");

        assertThat(json.get("searchType").asText()).isEqualTo("REGION");
        assertThat(json.get("regionNm").asText()).isEqualTo("화양동");
        // 화양동 실측 bbox: lat 37.538~37.549, lng 127.063~127.083
        assertThat(json.get("swLat").asDouble()).isBetween(37.5, 37.6);
        assertThat(json.get("neLat").asDouble()).isGreaterThan(json.get("swLat").asDouble());
        assertThat(json.get("swLng").asDouble()).isBetween(127.0, 127.1);
        assertThat(json.get("neLng").asDouble()).isGreaterThan(json.get("swLng").asDouble());
        // 중심좌표는 bbox 안에 있어야 한다
        assertThat(json.get("targetLat").asDouble())
                .isBetween(json.get("swLat").asDouble(), json.get("neLat").asDouble());
        assertThat(json.get("targetBuildingId").isNull()).isTrue();
    }

    @Test
    void 시군구명_검색은_REGION을_반환한다() throws Exception {
        JsonNode json = search("광진구");

        assertThat(json.get("searchType").asText()).isEqualTo("REGION");
        assertThat(json.get("regionNm").asText()).isEqualTo("광진구");
    }

    @Test
    void 숫자가_포함된_행정동명도_REGION을_반환한다() throws Exception {
        JsonNode json = search("역삼1동");

        assertThat(json.get("searchType").asText()).isEqualTo("REGION");
        assertThat(json.get("regionNm").asText()).isEqualTo("역삼1동");
    }

    @Test
    void 법정동명은_소속_행정동으로_매핑된다() throws Exception {
        // 자양동(법정동)은 행정동으로 자양1~4동으로 분할 - 지오코딩 좌표가 속한 행정동 하나로 매핑
        JsonNode json = search("자양동");

        // adstrd_nm은 "광진구 자양제4동" 형식(구 포함, '제N동' 표기)
        assertThat(json.get("searchType").asText()).isEqualTo("REGION");
        assertThat(json.get("regionNm").asText()).matches(".*자양제?[1-4]동");
        assertThat(json.get("swLat").asDouble()).isBetween(37.5, 37.6);
    }

    @Test
    void 지번주소_검색은_BUILDING을_반환한다() throws Exception {
        // 광진구 중곡동 18-1 (실데이터 존재 확인 완료, PNU 정확 매칭)
        JsonNode json = search("서울특별시 광진구 중곡동 18-1");

        assertThat(json.get("searchType").asText()).isEqualTo("BUILDING");
        assertThat(json.get("targetBuildingId").asText()).isNotBlank();
        assertThat(json.get("targetLat").asDouble()).isBetween(37.5, 37.6);
        assertThat(json.get("regionNm").isNull()).isTrue();
    }

    @Test
    void 도로명주소_검색은_BUILDING을_반환한다() throws Exception {
        // 광진구 용마산로 160 (실데이터 존재 확인 완료, 좌표 최근접 매칭)
        JsonNode json = search("서울특별시 광진구 용마산로 160");

        assertThat(json.get("searchType").asText()).isEqualTo("BUILDING");
        assertThat(json.get("targetBuildingId").asText()).isNotBlank();
    }

    @Test
    void 지역단위_검색어에_번지가_붙으면_NONE을_반환한다() throws Exception {
        // "광진구 1" - V-World가 광장동 1로 강제 완성하지만 검증에서 걸러져야 한다
        JsonNode json = search("광진구 1");

        assertThat(json.get("searchType").asText()).isEqualTo("NONE");
        assertThat(json.get("targetLat").isNull()).isTrue();
    }

    @Test
    void 의미없는_검색어는_NONE과_200을_반환한다() throws Exception {
        JsonNode json = search("존재하지않는검색어입니다");

        assertThat(json.get("searchType").asText()).isEqualTo("NONE");
    }
}
