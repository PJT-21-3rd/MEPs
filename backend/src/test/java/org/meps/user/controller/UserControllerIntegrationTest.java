package org.meps.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.config.ServletConfig;
import org.meps.user.dto.RefreshTokenDto;
import org.meps.user.dto.UserDto;
import org.meps.user.jwt.JwtProvider;
import org.meps.user.mapper.RefreshTokenMapper;
import org.meps.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class})
@Transactional
@DisplayName("회원 API 통합 테스트")
class UserControllerIntegrationTest {

    @Autowired private WebApplicationContext context;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserMapper userMapper;
    @Autowired private RefreshTokenMapper refreshTokenMapper;

    private MockMvc mockMvc;
    private String token;
    private Integer userId;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String BUILDING_ID = "1121510100100180001026332";
    /** { "refreshToken": "..." } 형태의 요청 본문 */
    private String refreshBody(String token) throws Exception {
        return objectMapper.writeValueAsString(Map.of("refreshToken", token));
    }
    /** 리프레시 토큰을 발급하고 DB에 저장한다 */
    private String issueRefreshToken() {
        String refreshToken = jwtProvider.createRefreshToken(userId);
        refreshTokenMapper.upsert(RefreshTokenDto.builder()
                .userId(userId)
                .token(refreshToken)
                .expiresAt(jwtProvider.getRefreshExpiresAt())
                .build());
        return refreshToken;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        UserDto user = UserDto.builder()
                .email("test-" + System.nanoTime() + "@meps.local")
                .password("encoded")
                .build();
        userMapper.insertUser(user);
        userId = user.getUserId();
        token = jwtProvider.createToken(userId);
    }

    private String bearer() {
        return "Bearer " + token;
    }

    @Test
    @DisplayName("회원 탈퇴 성공 → 204")
    void withdraw_success() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", bearer()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("탈퇴 시 찜도 삭제되고 saved_cnt 감소")
    void withdraw_removesSavedAndDecrementsCount() throws Exception {
        mockMvc.perform(post("/api/member/saved/{id}", BUILDING_ID)
                .header("Authorization", bearer()));
        int after = userMapper.findSavedCount(BUILDING_ID);

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", bearer()))
                .andExpect(status().isNoContent());

        assertThat(userMapper.findSavedCount(BUILDING_ID)).isEqualTo(after - 1);
        assertThat(userMapper.existsSavedBuilding(userId, BUILDING_ID)).isFalse();
    }

    @Test
    @DisplayName("토큰 없이 탈퇴 → 401")
    void withdraw_noToken() throws Exception {
        mockMvc.perform(delete("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("위조 토큰으로 탈퇴 → 401")
    void withdraw_invalidToken() throws Exception {
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer aaa.bbb.ccc"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- 토큰 재발급 ----------

    @Test
    @DisplayName("유효한 리프레시 토큰으로 재발급 → 200")
    void refresh_success() throws Exception {
        String refreshToken = issueRefreshToken();

        mockMvc.perform(post("/api/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("등록되지 않은 리프레시 토큰 → 401")
    void refresh_unknownToken() throws Exception {
        String notStored = jwtProvider.createRefreshToken(userId);   // DB에 저장 안 함

        mockMvc.perform(post("/api/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(notStored)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("위조된 리프레시 토큰 → 401")
    void refresh_invalidToken() throws Exception {
        mockMvc.perform(post("/api/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody("aaa.bbb.ccc")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("리프레시 토큰 누락 → 400")
    void refresh_blankToken() throws Exception {
        mockMvc.perform(post("/api/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody("")))
                .andExpect(status().isBadRequest());
    }

    // ---------- 로그아웃 ----------

    @Test
    @DisplayName("로그아웃 → 204, 리프레시 토큰 삭제")
    void logout_success() throws Exception {
        String refreshToken = issueRefreshToken();

        mockMvc.perform(post("/api/users/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isNoContent());

        assertThat(refreshTokenMapper.findByToken(refreshToken)).isNull();
    }

    @Test
    @DisplayName("로그아웃 후 재발급 → 401")
    void refresh_afterLogout() throws Exception {
        String refreshToken = issueRefreshToken();

        mockMvc.perform(post("/api/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(refreshToken)));

        mockMvc.perform(post("/api/users/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("이미 삭제된 토큰으로 로그아웃 → 204 (멱등)")
    void logout_twice() throws Exception {
        String refreshToken = issueRefreshToken();

        mockMvc.perform(post("/api/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshBody(refreshToken)));

        mockMvc.perform(post("/api/users/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshBody(refreshToken)))
                .andExpect(status().isNoContent());
    }
}