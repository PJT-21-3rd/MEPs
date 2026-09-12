package collectors.sinkhole;

import collectors.common.ApiClient;
import collectors.common.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 1단계: 지반침하 사고 목록 수집 (getSubsidenceList01)
 * - 연도 단위 기간 지정 (미지정 시 NODATA_ERROR)
 * - totalCount 기반 페이지네이션
 * - 서울특별시 필터 → data/sinkhole_list.json 저장
 */
public class SinkholeListCollector {

    private static final String BASE =
            "https://apis.data.go.kr/1613000/undergroundsafetyinfo01/getSubsidenceList01";
    private static final int YEAR_FROM = 2018;
    private static final int YEAR_TO = 2026;
    private static final int NUM_OF_ROWS = 100;

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String serviceKey = Config.get("molit.service-key");
        ArrayNode seoulItems = mapper.createArrayNode();
        int totalAll = 0;

        for (int year = YEAR_FROM; year <= YEAR_TO; year++) {
            int page = 1;
            int totalCount = -1;
            int fetched = 0;

            while (totalCount == -1 || fetched < totalCount) {
                String url = "%s?serviceKey=%s&pageNo=%d&numOfRows=%d&type=json&sagoDateFrom=%d0101&sagoDateTo=%d1231"
                        .formatted(BASE, serviceKey, page, NUM_OF_ROWS, year, year);

                JsonNode root = mapper.readTree(ApiClient.get(url)).path("response");
                String resultCode = root.path("header").path("resultCode").asText();

                if ("03".equals(resultCode) || "3".equals(resultCode)) {   // NODATA
                    System.out.printf("%d: no data%n", year);
                    break;
                }
                if (!"0".equals(resultCode) && !"00".equals(resultCode)) {
                    throw new RuntimeException("API error [%s] year=%d page=%d"
                            .formatted(root.path("header").path("resultMsg").asText(), year, page));
                }

                totalCount = Integer.parseInt(root.path("totalCount").asText("0"));
                JsonNode items = root.path("body").path("items");

                for (JsonNode item : items) {
                    fetched++;
                    if ("서울특별시".equals(item.path("sido").asText())) {
                        seoulItems.add(item);
                    }
                }
                System.out.printf("%d: page %d done (%d/%d)%n", year, page, fetched, totalCount);
                page++;
                ApiClient.sleep(200);
            }
            totalAll += fetched;
        }

        Path out = Path.of("data", "sinkhole_list.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(seoulItems));

        System.out.printf("%n전국 %d건 중 서울 %d건 → %s%n", totalAll, seoulItems.size(), out.toAbsolutePath());
    }
}