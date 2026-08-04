package org.meps.common.geocoding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import org.springframework.test.web.client.RequestMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * V-World를 실제 호출하지 않고 MockRestServiceServer로 응답을 재현한다.
 * 응답 JSON은 2026-08-02 실측값 기반.
 */
class VWorldGeocodingClientTest {

    private MockRestServiceServer server;
    private VWorldGeocodingClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new VWorldGeocodingClient(
                "test-key", "http://localhost:8080", new ObjectMapper(), restTemplate);
    }

    private static RequestMatcher typeIs(String type) {
        return request -> assertThat(request.getURI().toString()).contains("type=" + type);
    }

    private static final String NOT_FOUND_JSON = """
            {"response": {"status": "NOT_FOUND"}}
            """;

    private static String okJson(String level4L, String level4LC, String level5,
                                 String refinedText, double x, double y) {
        return """
                {"response": {
                  "status": "OK",
                  "refined": {
                    "text": "%s",
                    "structure": {"level4L": "%s", "level4LC": "%s", "level5": "%s"}
                  },
                  "result": {"point": {"x": "%s", "y": "%s"}}
                }}
                """.formatted(refinedText, level4L, level4LC, level5, x, y);
    }

    @Test
    void 지번주소는_PARCEL_매칭으로_PNU까지_반환한다() {
        server.expect(method(HttpMethod.GET))
                .andExpect(typeIs("PARCEL"))
                .andRespond(withSuccess(okJson(
                                "군자동", "1121510900103610023", "361-23",
                                "서울특별시 광진구 군자동 361-23", 127.0793, 37.5542),
                        MediaType.APPLICATION_JSON));

        GeocodeResult result = client.geocode("서울 광진구 군자동 361-23");

        assertThat(result).isNotNull();
        assertThat(result.getPnu()).isEqualTo("1121510900103610023");
        assertThat(result.getLat()).isEqualTo(37.5542);
        assertThat(result.getLng()).isEqualTo(127.0793);
        server.verify();
    }

    @Test
    void 도로명주소는_ROAD_우선_매칭되고_PNU는_null이다() {
        server.expect(method(HttpMethod.GET))
                .andExpect(typeIs("ROAD"))
                .andRespond(withSuccess(okJson(
                                "능동로", "", "120",
                                "서울특별시 광진구 능동로 120 (화양동)", 127.0736, 37.5427),
                        MediaType.APPLICATION_JSON));

        GeocodeResult result = client.geocode("서울 광진구 능동로 120");

        assertThat(result).isNotNull();
        assertThat(result.getPnu()).isNull();
        assertThat(result.getLat()).isEqualTo(37.5427);
        server.verify();
    }

    @Test
    void 강제_완성된_응답은_불신하고_null을_반환한다() {
        // "서울시 1" - V-World가 논현동/왕산로를 지어내 OK를 반환하는 실측 케이스
        server.expect(method(HttpMethod.GET))
                .andExpect(typeIs("PARCEL"))
                .andRespond(withSuccess(okJson(
                                "논현동", "1168010800100010000", "1",
                                "서울특별시 강남구 논현동 1", 127.0202, 37.5159),
                        MediaType.APPLICATION_JSON));
        server.expect(method(HttpMethod.GET))
                .andExpect(typeIs("ROAD"))
                .andRespond(withSuccess(okJson(
                                "왕산로", "", "1",
                                "서울특별시 동대문구 왕산로 1 (신설동)", 127.0247, 37.5760),
                        MediaType.APPLICATION_JSON));

        assertThat(client.geocode("서울시 1")).isNull();
        server.verify();
    }

    @Test
    void 지어낸_번지가_입력에_없으면_불신한다() {
        // "능동로" 단독 - 김해시 능동로 10으로 강제 완성되는 실측 케이스 (10이 입력에 없음)
        server.expect(method(HttpMethod.GET))
                .andExpect(typeIs("PARCEL"))
                .andRespond(withSuccess(NOT_FOUND_JSON, MediaType.APPLICATION_JSON));
        server.expect(method(HttpMethod.GET))
                .andExpect(typeIs("ROAD"))
                .andRespond(withSuccess(okJson(
                                "능동로", "", "10",
                                "경상남도 김해시 능동로 10", 128.8112, 35.1937),
                        MediaType.APPLICATION_JSON));

        assertThat(client.geocode("능동로")).isNull();
        server.verify();
    }

    @Test
    void 두_타입_모두_NOT_FOUND면_null을_반환한다() {
        server.expect(method(HttpMethod.GET))
                .andRespond(withSuccess(NOT_FOUND_JSON, MediaType.APPLICATION_JSON));
        server.expect(method(HttpMethod.GET))
                .andRespond(withSuccess(NOT_FOUND_JSON, MediaType.APPLICATION_JSON));

        assertThat(client.geocode("존재하지않는주소 999999")).isNull();
        server.verify();
    }

    @Test
    void ERROR_응답은_GeocodingException을_던진다() {
        server.expect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"response\": {\"status\": \"ERROR\", \"error\": {\"text\": \"인증키 오류\"}}}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.geocode("서울 광진구 군자동 361-23"))
                .isInstanceOf(GeocodingException.class)
                .hasMessageContaining("ERROR");
    }

    @Test
    void HTTP_5xx는_GeocodingException을_던진다() {
        server.expect(method(HttpMethod.GET))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.geocode("서울 광진구 군자동 361-23"))
                .isInstanceOf(GeocodingException.class);
    }
}
