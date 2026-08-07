package collectors.buildings;

import collectors.common.ApiClient;
import collectors.common.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * VWorld 토지특성 조회 (NED)
 * http://api.vworld.kr/ned/data/getLandCharacteristics
 *
 * 담당 컬럼: lndcgr_code_nm(지목), prpos_area_nm(용도지역),
 *           road_side_code_nm(도로조건), pblntf_pclnd(공시지가)
 */

public class LandCharClient {

    private static final String BASE_URL =
            "http://api.vworld.kr/ned/data/getLandCharacteristics";
    private static final ObjectMapper OM = new ObjectMapper();

    public static JsonNode fetch(String pnu) {
        String url = BASE_URL
                + "?key=" + Config.get("vworld.key")
                + "&domain=" + Config.get("vworld.domain")
                + "&pnu=" + pnu
                + "&format=json"
                + "&numOfRows=30"
                + "&pageNo=1";

        try {
            JsonNode root = OM.readTree(ApiClient.get(url));
            JsonNode fields = root.path("landCharacteristicss").path("field");

            if (fields.isMissingNode() || fields.isNull()) return null;

            // 1건이면 객체, 여러 건이면 배열
            if (!fields.isArray()) return fields;

            JsonNode latest = null;
            int latestYear = -1;
            for (JsonNode f : fields) {
                int year = intOr(f, "stdrYear", -1);
                if (year > latestYear) {
                    latestYear = year;
                    latest = f;
                }
            }
            return latest;

        } catch (Exception e) {
            throw new RuntimeException("토지특성 파싱 실패 (pnu=" + pnu + "): " + e.getMessage(), e);
        }
    }


    public static String lndcgrCodeNm(JsonNode item)   { return text(item, "lndcgrCodeNm"); }
    public static String prposAreaNm(JsonNode item)    { return text(item, "prposArea1Nm"); }
    public static String roadSideCodeNm(JsonNode item) { return text(item, "roadSideCodeNm"); }

    public static Long pblntfPclnd(JsonNode item) {
        String s = text(item, "pblntfPclnd");
        if (s == null) return null;
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String text(JsonNode item, String field) {
        if (item == null) return null;
        JsonNode v = item.get(field);
        if (v == null || v.isNull()) return null;
        String s = v.asText().trim();
        return s.isEmpty() ? null : s;
    }

    private static int intOr(JsonNode item, String field, int def) {
        JsonNode v = item.get(field);
        if (v == null || v.isNull()) return def;
        try {
            return Integer.parseInt(v.asText().trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private LandCharClient() {}
}