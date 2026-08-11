package collectors.buildings;

import collectors.common.ApiClient;
import collectors.common.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * V-World 토지임야목록 조회 (NED)
 * https://api.vworld.kr/ned/data/ladfrlList
 *
 * 담당 컬럼: plat_area (대지면적)
 *
 * 건축물대장 표제부의 platArea는 0으로 등록된 건물이 많아
 * 이 API의 lndpclAr(토지면적)을 대지면적으로 사용한다.
 *
 * pnu로 바로 조회 가능 (PnuParser 불필요).
 *
 * 주의: 응답 구조가 ladfrlVOList 안에 같은 이름의 배열이 한 번 더 중첩된다.
 *      { "ladfrlVOList": { "ladfrlVOList": [ {...} ] } }
 */
public class LadfrlListClient {

    private static final String BASE_URL = "https://api.vworld.kr/ned/data/ladfrlList";
    private static final ObjectMapper OM = new ObjectMapper();

    /**
     * 필지의 토지면적 조회.
     *
     * @return 면적(㎡). 데이터 없으면 null
     */
    public static Double fetchArea(String pnu) {
        String url = BASE_URL
                + "?key=" + Config.get("vworld.key")
                + "&domain=" + Config.get("vworld.domain")
                + "&pnu=" + pnu
                + "&numOfRows=1"
                + "&pageNo=1"
                + "&format=json";

        try {
            JsonNode root = OM.readTree(ApiClient.get(url));
            JsonNode list = root.path("ladfrlVOList").path("ladfrlVOList");

            if (!list.isArray() || list.size() == 0) return null;

            return doubleVal(list.get(0), "lndpclAr");

        } catch (Exception e) {
            throw new RuntimeException("토지임야목록 파싱 실패 (pnu=" + pnu + "): "
                    + e.getMessage(), e);
        }
    }

    private static Double doubleVal(JsonNode item, String field) {
        JsonNode v = item.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText().trim();
        if (s.isEmpty()) return null;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LadfrlListClient() {}
}
