package collectors.buildings;

import collectors.common.ApiClient;
import collectors.common.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * VWorld WFS 건물통합정보 조회
 * http://api.vworld.kr/req/wfs (TYPENAME=lt_c_bldginfo)
 *
 * 담당 컬럼: bd_mgt_sn(PK), pnu, footprint, viol_bd_yn
 *
 */

public class BuildingWfsClient {

    private static final String BASE_URL = "http://api.vworld.kr/req/wfs";
    private static final ObjectMapper OM = new ObjectMapper();

    public static List<JsonNode> fetch(String bbox) {
        String url = BASE_URL
                + "?SERVICE=WFS"
                + "&REQUEST=GetFeature"
                + "&TYPENAME=lt_c_bldginfo"
                + "&KEY=" + Config.get("vworld.key")
                + "&DOMAIN=" + Config.get("vworld.domain")
                + "&BBOX=" + bbox
                + "&SRSNAME=EPSG:4326"
                + "&OUTPUT=application/json"
                + "&MAXFEATURES=1000";

        try {
            JsonNode root = OM.readTree(ApiClient.get(url));
            JsonNode features = root.get("features");

            List<JsonNode> result = new ArrayList<>();
            if (features == null || !features.isArray()) return result;

            for (JsonNode feature : features) {
                if (isRegistered(feature)) result.add(feature);
            }
            return result;

        } catch (Exception e) {
            throw new RuntimeException("WFS 파싱 실패: " + e.getMessage(), e);
        }
    }

    private static boolean isRegistered(JsonNode feature) {
        JsonNode props = feature.get("properties");
        JsonNode bldrgstPk = props.get("bldrgst_pk");
        JsonNode bdMgtSn   = props.get("bd_mgt_sn");

        return bldrgstPk != null && !bldrgstPk.isNull()
                && bdMgtSn != null && !bdMgtSn.isNull();
    }

    public static String bdMgtSn(JsonNode feature) {
        return feature.get("properties").get("bd_mgt_sn").asText();
    }

    public static String pnu(JsonNode feature) {
        return feature.get("properties").get("pnu").asText();
    }


    public static String violBdYn(JsonNode feature) {
        JsonNode v = feature.get("properties").get("viol_bd_yn");
        if (v == null || v.isNull()) return null;
        return "1".equals(v.asText()) ? "Y" : "N";
    }


    public static String geometry(JsonNode feature) {
        return feature.get("geometry").toString();
    }

    private BuildingWfsClient() {}
}