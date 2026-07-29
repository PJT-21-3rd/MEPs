package collectors.sinkhole;

import collectors.common.ApiClient;
import collectors.common.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 2단계: 사고번호(sagoNo)별 상세 조회 (getSubsidenceInfo01)
 * 입력: data/sinkhole_list.json (168건)
 * 출력: data/sinkhole_detail.json (목록+상세 병합본)
 */
public class SinkholeDetailCollector {

    private static final String BASE =
            "https://apis.data.go.kr/1613000/undergroundsafetyinfo01/getSubsidenceInfo01";

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        String serviceKey = Config.get("molit.service-key");

        JsonNode list = mapper.readTree(Files.readString(Path.of("data", "sinkhole_list.json")));
        ArrayNode merged = mapper.createArrayNode();
        int noDetail = 0;

        for (int i = 0; i < list.size(); i++) {
            String sagoNo = list.get(i).path("sagoNo").asText();
            String url = "%s?serviceKey=%s&type=json&sagoNo=%s".formatted(BASE, serviceKey, sagoNo);

            JsonNode root = mapper.readTree(ApiClient.get(url)).path("response");
            JsonNode items = root.path("body").path("items");

            // items가 배열이 아닌 단일 객체로 오는 경우 방어
            JsonNode detail = items.isArray()
                    ? (items.size() > 0 ? items.get(0) : null)
                    : (items.isObject() ? items : null);

            if (detail == null) {
                System.err.printf("[%d/%d] %s: 상세 없음%n", i + 1, list.size(), sagoNo);
                noDetail++;
                merged.add(list.get(i).deepCopy());   // 목록 정보만이라도 유지
            } else {
                ObjectNode row = list.get(i).deepCopy();
                row.setAll((ObjectNode) detail);      // 상세가 목록 필드 덮어씀 (동일 키는 값 같음)
                merged.add(row);
            }

            if ((i + 1) % 20 == 0) System.out.printf("[%d/%d] 진행 중…%n", i + 1, list.size());
            ApiClient.sleep(200);
        }

        Path out = Path.of("data", "sinkhole_detail.json");
        Files.writeString(out, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(merged));

        System.out.printf("%n완료: %d건 (상세 미확보 %d건) → %s%n",
                merged.size(), noDetail, out.toAbsolutePath());
    }
}