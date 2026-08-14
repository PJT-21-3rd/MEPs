package org.meps.common.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.building.exception.BuildingNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GlobalExceptionHandler가 예외 종류별로 상태코드와 {code, message} 응답 바디를
 * 올바르게 조립하는지 검증. DB/전체 스프링 컨텍스트 없이 standalone MockMvc로만 확인한다.
 */
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @RestController
    static class TestController {

        @GetMapping("/test/business-exception")
        void businessException() {
            throw new BuildingNotFoundException("1111012345000000000000001");
        }

        @GetMapping("/test/missing-param")
        void missingParam(@RequestParam String required) {
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new RuntimeException("boom");
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    @DisplayName("BusinessException → ErrorCode 상태코드 + {code} 바디 (message는 응답에 담지 않음)")
    void businessException_returnsErrorCodeOnly() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isNotFound())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("code").asText()).isEqualTo("BUILDING_NOT_FOUND");
        assertThat(body.has("message")).isFalse();
    }

    @Test
    @DisplayName("필수 파라미터 누락 → 400 + INVALID_INPUT_VALUE")
    void missingRequestParameter_returnsInvalidInputValue() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/missing-param"))
                .andExpect(status().isBadRequest())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("code").asText()).isEqualTo("INVALID_INPUT_VALUE");
    }

    @Test
    @DisplayName("미등록 예외 → 500 + INTERNAL_SERVER_ERROR")
    void unexpectedException_returnsInternalServerError() throws Exception {
        MvcResult result = mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("code").asText()).isEqualTo("INTERNAL_SERVER_ERROR");
    }
}
