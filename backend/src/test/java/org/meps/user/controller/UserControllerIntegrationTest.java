package org.meps.user.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.config.ServletConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextHierarchy({
        @ContextConfiguration(classes = RootConfig.class),
        @ContextConfiguration(classes = ServletConfig.class)
})
@DisplayName("회원가입 API")
class UserControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @Transactional
    @DisplayName("정상 요청은 201을 반환한다")
    void signup_success() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"new@test.com\",\"password\":\"Test1234!\",\"passwordConfirm\":\"Test1234!\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("이메일 형식이 잘못되면 400을 반환한다")
    void signup_invalidEmail() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"notanemail\",\"password\":\"Test1234!\",\"passwordConfirm\":\"Test1234!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 400을 반환한다")
    void signup_passwordTooShort() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"short@test.com\",\"password\":\"Ab1!\",\"passwordConfirm\":\"Ab1!\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호에 특수문자가 없으면 400을 반환한다")
    void signup_passwordWithoutSpecialChar() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nospecial@test.com\",\"password\":\"Test12345\",\"passwordConfirm\":\"Test12345\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 확인이 일치하지 않으면 400을 반환한다")
    void signup_passwordMismatch() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"mismatch@test.com\",\"password\":\"Test1234!\",\"passwordConfirm\":\"Test5678!\"}"))
                .andExpect(status().isBadRequest());
    }
}