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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {RootConfig.class, ServletConfig.class})
@Transactional
@DisplayName("회원 API 통합 테스트")
class UserControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private JwtProvider jwtProvider;
    @Autowired private UserMapper userMapper;

    private MockMvc mockMvc;
    private String token;
    private Integer userId;

    private static final String BUILDING_ID = "1121510100100180001026332";

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
}