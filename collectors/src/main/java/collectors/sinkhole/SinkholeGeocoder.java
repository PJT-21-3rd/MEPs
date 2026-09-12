package collectors.sinkhole;

import collectors.common.ApiClient;
import collectors.common.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 3~4단계: 정제(dong 결측 제외) + V-World 지오코딩
 * 입력: data/sinkhole_detail.json (168건)
 * 출력: data/sinkhole_geocoded.json (158건 예상, 최종 적재본)
 *
 * 전략:
 *  1차) 지번주소(PARCEL) 지오코딩(getcoord) → 좌표 확보
 *       + 리버스 지오코딩(getaddress) → 법정동코드 확보 → PNU 조립
 *  2차) 실패 시 동 단위 폴백 → 좌표만 확보, pnu = null
 */
public class SinkholeGeocoder {

    private static final String GEOCODER = "https://api.vworld.kr/req/address";
    private static final String DOMAIN =
            URLEncoder.encode("http://43.203.87.156:8080", StandardCharsets.UTF_8);

    // "147-2앞 (봉은사로 86길)" → 산 여부 + 본번 + 부번 추출
    private static final Pattern JIBUN = Pattern.compile("(산)?\\s*(\\d{1,4})(?:-(\\d{1,4}))?");

    // API dong 필드에 행정동명이 섞여 온 케이스 수동 보정 (행정동명 → 법정동명)
    private static final Map<String, String> DONG_FIX = Map.of(
            "관악구|보라매동", "봉천동"
    );

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String key = Config.get("vworld.key");

        JsonNode list = mapper.readTree(Files.readString(Path.of("data", "sinkhole_detail.json")));
        ArrayNode out = mapper.createArrayNode();
        int excluded = 0, parcelOk = 0, pnuOk = 0, dongFallback = 0, failed = 0;

        for (JsonNode item : list) {
            String dong = item.path("dong").asText().trim();
            if (dong.isEmpty()) { excluded++; continue; }          // 3단계: dong 결측 제외

            String sigungu = item.path("sigungu").asText().trim();
            String addr = item.path("addr").asText().trim();

            // 행정동명 → 법정동명 수동 보정
            String fixed = DONG_FIX.get(sigungu + "|" + dong);
            if (fixed != null) dong = fixed;

            ObjectNode row = mapper.createObjectNode();
            row.put("sago_no", item.path("sagoNo").asText());
            row.put("sgg_nm", sigungu);
            row.put("bjd_nm", dong);
            row.put("sago_date", item.path("sagoDate").asText());

            // 1차: 지번주소 지오코딩
            Matcher m = JIBUN.matcher(addr);
            JsonNode geo = null;
            String bun = null, ji = null;
            boolean san = false;

            if (m.find()) {
                san = m.group(1) != null;
                bun = m.group(2);
                ji = m.group(3) != null ? m.group(3) : "0";
                String jibunAddr = "서울특별시 %s %s %s%s%s".formatted(
                        sigungu, dong, san ? "산" : "", bun, "0".equals(ji) ? "" : "-" + ji);
                geo = geocode(key, jibunAddr);
            }

            if (geo != null) {
                parcelOk++;
                double lon = geo.path("result").path("point").path("x").asDouble();
                double lat = geo.path("result").path("point").path("y").asDouble();
                row.put("lon", lon);
                row.put("lat", lat);

                // 리버스 지오코딩으로 법정동코드 확보 → PNU 조립
                String bjdCd = reverseBjdCode(key, lon, lat);
                if (bjdCd != null) {
                    pnuOk++;
                    row.put("pnu", "%s%s%04d%04d".formatted(
                            bjdCd, san ? "2" : "1", Integer.parseInt(bun), Integer.parseInt(ji)));
                } else {
                    row.putNull("pnu");
                }
                ApiClient.sleep(100);
            } else {
                // 2차 폴백: 동 단위
                geo = geocode(key, "서울특별시 %s %s".formatted(sigungu, dong));
                if (geo != null) {
                    dongFallback++;
                    row.put("lon", geo.path("result").path("point").path("x").asDouble());
                    row.put("lat", geo.path("result").path("point").path("y").asDouble());
                    row.putNull("pnu");
                } else {
                    failed++;
                    System.err.printf("지오코딩 실패: %s %s %s (%s)%n",
                            row.path("sago_no").asText(), sigungu, dong, addr);
                    continue;
                }
            }
            out.add(row);
            ApiClient.sleep(100);
        }

        Path outPath = Path.of("data", "sinkhole_geocoded.json");
        Files.writeString(outPath, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(out));

        System.out.printf("%n제외 %d / 지번 성공 %d (PNU 조립 %d) / 동 폴백 %d / 실패 %d → 총 %d건 저장: %s%n",
                excluded, parcelOk, pnuOk, dongFallback, failed, out.size(), outPath.toAbsolutePath());
    }

    /** 지오코딩(주소→좌표). 성공 시 response 노드, 실패 시 null */
    private static JsonNode geocode(String key, String address) {
        try {
            String url = "%s?service=address&request=getcoord&version=2.0&crs=epsg:4326&type=PARCEL&refine=true&simple=false&format=json&key=%s&domain=%s&address=%s"
                    .formatted(GEOCODER, key, DOMAIN,
                            URLEncoder.encode(address, StandardCharsets.UTF_8));
            JsonNode res = mapper.readTree(ApiClient.get(url)).path("response");
            return "OK".equals(res.path("status").asText()) ? res : null;
        } catch (Exception e) {
            return null;
        }
    }

    /** 리버스 지오코딩(좌표→주소): 법정동코드(10자리) 반환. 실패 시 null */
    private static String reverseBjdCode(String key, double lon, double lat) {
        try {
            String url = "%s?service=address&request=getaddress&version=2.0&crs=epsg:4326&type=PARCEL&simple=false&format=json&key=%s&domain=%s&point=%s,%s"
                    .formatted(GEOCODER, key, DOMAIN, lon, lat);
            JsonNode res = mapper.readTree(ApiClient.get(url)).path("response");
            if (!"OK".equals(res.path("status").asText())) return null;
            String code = res.path("result").get(0).path("structure").path("level4LC").asText();
            return code.length() == 10 ? code : null;
        } catch (Exception e) {
            return null;
        }
    }
}