package org.meps.common.geocoding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * V-World 지오코더(getcoord) 클라이언트
 *
 * - 도로명/지번 자동 판별: "~로 12", "~길 12" 패턴이면 ROAD 우선, 아니면 PARCEL 우선
 * - V-World는 지역 단위 입력도 임의 필지/도로로 강제 완성해 OK를 반환하므로
 *   ("서울시 1" → 강남구 논현동 1, "능동로" → 김해시 능동로 10),
 *   지오코더가 채운 동명/도로명·번지가 입력에 실제로 있을 때만 결과를 신뢰한다
 * - 결과 없음(NOT_FOUND·검증 탈락)은 null, 그 외 실패(HTTP 오류·ERROR 응답·타임아웃)는 GeocodingException(502)
 */
@Slf4j
@Component
public class VWorldGeocodingClient {

    private static final String ENDPOINT = "https://api.vworld.kr/req/address";
    private static final Pattern ROAD_HINT = Pattern.compile(".*[로길]\\s*\\d.*");
    private static final Pattern PNU_19 = Pattern.compile("\\d{19}");

    private final String apiKey;
    private final String domain;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public VWorldGeocodingClient(@Value("${vworld.key}") String apiKey,
                                 @Value("${vworld.domain}") String domain,
                                 ObjectMapper objectMapper,
                                 RestTemplate restTemplate) {
        this.apiKey = apiKey;
        this.domain = domain;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }

    /** 주소 → 좌표(+PNU). 결과가 없거나 입력과 불일치하면 null */
    public GeocodeResult geocode(String keyword) {
        String[] types = ROAD_HINT.matcher(keyword).matches()
                ? new String[]{"ROAD", "PARCEL"}
                : new String[]{"PARCEL", "ROAD"};

        for (String type : types) {
            JsonNode response = request(keyword, type);
            String status = response.path("status").asText();
            if ("OK".equals(status)) {
                GeocodeResult result = toResult(keyword, response, type);
                if (result != null) {
                    return result;
                }
                log.debug("지오코딩 강제 완성 감지 - 결과 불신: keyword={}, refined={}",
                        keyword, response.path("refined").path("text").asText());
            } else if (!"NOT_FOUND".equals(status)) {
                throw new GeocodingException("V-World 응답 오류: status=" + status
                        + ", error=" + response.path("error").path("text").asText());
            }
        }
        return null;
    }

    private JsonNode request(String keyword, String type) {
        URI uri = UriComponentsBuilder.fromHttpUrl(ENDPOINT)
                .queryParam("service", "address")
                .queryParam("request", "getcoord")
                .queryParam("version", "2.0")
                .queryParam("crs", "epsg:4326")
                .queryParam("type", type)
                .queryParam("refine", "true")
                .queryParam("simple", "false")
                .queryParam("format", "json")
                .queryParam("key", apiKey)
                .queryParam("domain", domain)
                .queryParam("address", keyword)
                .encode(StandardCharsets.UTF_8)
                .build()
                .toUri();
        try {
            String body = restTemplate.getForObject(uri, String.class);
            return objectMapper.readTree(body).path("response");
        } catch (RestClientResponseException e) {
            throw new GeocodingException("V-World HTTP 오류: " + e.getRawStatusCode(), e);
        } catch (RestClientException e) {
            throw new GeocodingException("V-World 호출 실패: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new GeocodingException("V-World 응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    /** 강제 완성 검증을 통과하면 결과로 변환, 탈락하면 null */
    private GeocodeResult toResult(String keyword, JsonNode response, String type) {
        JsonNode structure = response.path("refined").path("structure");
        if (!matchesKeyword(keyword, structure)) {
            return null;
        }

        String pnu = null;
        if ("PARCEL".equals(type)) {
            // getcoord PARCEL 응답의 level4LC는 법정동코드가 아니라 완성된 PNU 19자리
            // (리버스 지오코딩 getaddress는 10자리 법정동코드 - 혼동 주의)
            String level4LC = structure.path("level4LC").asText();
            if (PNU_19.matcher(level4LC).matches()) {
                pnu = level4LC;
            }
        }

        JsonNode point = response.path("result").path("point");
        return GeocodeResult.builder()
                .lat(point.path("y").asDouble())
                .lng(point.path("x").asDouble())
                .pnu(pnu)
                .build();
    }

    /**
     * 지오코더가 채워 넣은 level4L(PARCEL=법정동명, ROAD=도로명)과
     * level5의 본번(번지/건물번호)이 입력에 실제로 포함되어 있는지 검사한다.
     */
    private boolean matchesKeyword(String keyword, JsonNode structure) {
        String level4L = structure.path("level4L").asText();
        String level5 = structure.path("level5").asText();
        if (level4L.isEmpty() || level5.isEmpty()) {
            return false;
        }
        String bun = level5.split("-")[0].replaceAll("\\D", "");
        return keyword.contains(level4L) && !bun.isEmpty() && keyword.contains(bun);
    }
}
