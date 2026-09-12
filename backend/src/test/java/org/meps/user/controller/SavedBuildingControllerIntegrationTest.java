package org.meps.user.controller;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class})
@Transactional
@DisplayName("찜하기 API 통합 테스트")
class SavedBuildingControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserMapper userMapper;

    private MockMvc mockMvc;
    private String token;

    /** 실제 DB에 존재하는 건물관리번호 */
    private static final String BUILDING_ID = "1121510100100180001026332";

    /** 존재하지 않는 건물관리번호 (25자리) */
    private static final String UNKNOWN_BUILDING_ID = "0000000000000000000000000";

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

    // ---------- 등록 ----------

    @Test
    @DisplayName("찜하기 등록 성공 → 201")
    void save_success() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                        .header("Authorization", bearer()))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("중복 찜 → 409")
    void save_duplicate() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));

        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                        .header("Authorization", bearer()))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("존재하지 않는 건물 → 404")
    void save_buildingNotFound() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", UNKNOWN_BUILDING_ID)
                        .header("Authorization", bearer()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("토큰 없음 → 401")
    void save_noToken() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Bearer 접두사 없음 → 401")
    void save_malformedHeader() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                        .header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("위조 토큰 → 401")
    void save_invalidToken() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                        .header("Authorization", "Bearer aaa.bbb.ccc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("찜 등록 시 saved_cnt 증가")
    void save_incrementsCount() throws Exception {
        int before = userMapper.findSavedCount(BUILDING_ID);

        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));

        assertThat(userMapper.findSavedCount(BUILDING_ID)).isEqualTo(before + 1);
    }

    // ---------- 해제 ----------

    @Test
    @DisplayName("찜하기 해제 성공 → 204")
    void unsave_success() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));

        mockMvc.perform(delete("/api/member/saved/{id}", BUILDING_ID)
                        .header("Authorization", bearer()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("찜하지 않은 건물 해제 → 204 (멱등)")
    void unsave_notSaved() throws Exception {
        mockMvc.perform(delete("/api/member/saved/{id}", BUILDING_ID)
                        .header("Authorization", bearer()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("해제 시 토큰 없음 → 401")
    void unsave_noToken() throws Exception {
        mockMvc.perform(delete("/api/member/saved/{id}", BUILDING_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("찜 해제 시 saved_cnt 감소")
    void unsave_decrementsCount() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));
        int after = userMapper.findSavedCount(BUILDING_ID);

        mockMvc.perform(delete("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));

        assertThat(userMapper.findSavedCount(BUILDING_ID)).isEqualTo(after - 1);
    }

    // ---------- 사이클 ----------

    @Test
    @DisplayName("등록 → 해제 → 재등록")
    void save_afterUnsave() throws Exception {
        String url = "/api/member/saved/" + BUILDING_ID;

        mockMvc.perform(post(url).header("Authorization", bearer()))
                .andExpect(status().isCreated());
        mockMvc.perform(delete(url).header("Authorization", bearer()))
                .andExpect(status().isNoContent());
        mockMvc.perform(post(url).header("Authorization", bearer()))
                .andExpect(status().isCreated());
    }

    // ---------- 목록 조회 ----------

    @Test
    @DisplayName("찜 목록 조회 성공")
    void list_success() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));

        String body = mockMvc.perform(get("/api/member/saved")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).contains("\"buildingId\":\"" + BUILDING_ID + "\"");
        assertThat(body).contains("\"roadAddr\"");
        assertThat(body).contains("\"jibunAddr\"");
        assertThat(body).contains("\"lat\"");
        assertThat(body).contains("\"lng\"");
        assertThat(body).contains("\"archArea\"");
        assertThat(body).contains("\"mainPurpsNm\"");
        assertThat(body).contains("\"grndFlr\"");
        assertThat(body).contains("\"ugrndFlr\"");
        assertThat(body).contains("\"useAprDay\"");
        assertThat(body).contains("\"saved\":true");
    }

    @Test
    @DisplayName("찜한 매물이 없으면 빈 배열 반환")
    void list_empty() throws Exception {
        String body = mockMvc.perform(get("/api/member/saved")
                        .header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body).isEqualTo("[]");
    }

    @Test
    @DisplayName("찜 목록 조회 시 토큰 없음")
    void list_noToken() throws Exception {
        mockMvc.perform(get("/api/member/saved"))
                .andExpect(status().isUnauthorized());
    }
}