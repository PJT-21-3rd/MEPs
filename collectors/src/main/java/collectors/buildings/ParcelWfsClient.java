package collectors.buildings;

import collectors.common.ApiClient;
import collectors.common.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * VWorld 연속지적도 조회 (Data API)
 * http://api.vworld.kr/req/data (data=LP_PA_CBND_BUBUN)
 *
 * 담당 컬럼: parcel_geom (MULTIPOLYGON, SRID 4326)
 *
 */

public class ParcelWfsClient {

    private static final String BASE_URL = "http://api.vworld.kr/req/data";
    private static final ObjectMapper OM = new ObjectMapper();

    public static String fetchGeometry(String pnu) {
        String url = BASE_URL
                + "?service=data"
                + "&request=GetFeature"
                + "&data=LP_PA_CBND_BUBUN"
                + "&key=" + Config.get("vworld.key")
                + "&domain=" + Config.get("vworld.domain")
                + "&attrFilter=pnu:=:" + pnu
                + "&crs=EPSG:4326"
                + "&format=json"
                + "&size=10";

        try {
            JsonNode root = OM.readTree(ApiClient.get(url));

            String status = root.path("response").path("status").asText("");
            if ("NOT_FOUND".equals(status)) return null;
            if (!"OK".equals(status)) {
                throw new RuntimeException("VWorld 응답 status=" + status);
            }

            JsonNode features = root.path("response").path("result")
                    .path("featureCollection").path("features");

            if (!features.isArray() || features.size() == 0) return null;

            JsonNode geometry = features.get(0).get("geometry");
            return geometry == null ? null : geometry.toString();

        } catch (Exception e) {
            throw new RuntimeException("연속지적도 파싱 실패 (pnu=" + pnu + "): " + e.getMessage(), e);
        }
    }

    private ParcelWfsClient() {}
}