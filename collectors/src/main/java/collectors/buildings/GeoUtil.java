package collectors.buildings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GeoJSON 좌표 계산 유틸.
 *
 * MySQL의 ST_Centroid는 지리 좌표계(SRID 4326)를 지원하지 않으므로
 * ("st_centroid(MULTIPOLYGON) has not been implemented for geographic
 *  spatial reference systems") 중심점을 자바에서 계산한다.
 */
public class GeoUtil {

    private static final ObjectMapper OM = new ObjectMapper();

    public static String centerOf(String geoJson) {
        if (geoJson == null) return null;

        try {
            JsonNode coords = OM.readTree(geoJson).get("coordinates");
            if (coords == null || !coords.isArray()) return null;

            double[] box = {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
            collect(coords, box);

            if (box[0] > box[2]) return null;   // 좌표를 하나도 못 찾음

            double lon = (box[0] + box[2]) / 2;
            double lat = (box[1] + box[3]) / 2;
            return String.format("{\"type\":\"Point\",\"coordinates\":[%.7f,%.7f]}", lon, lat);

        } catch (Exception e) {
            return null;
        }
    }

    private static void collect(JsonNode node, double[] box) {
        if (!node.isArray() || node.size() == 0) return;

        JsonNode first = node.get(0);
        if (first.isNumber() && node.size() >= 2) {
            double lon = node.get(0).asDouble();
            double lat = node.get(1).asDouble();
            if (lon < box[0]) box[0] = lon;
            if (lat < box[1]) box[1] = lat;
            if (lon > box[2]) box[2] = lon;
            if (lat > box[3]) box[3] = lat;
            return;
        }
        for (JsonNode child : node) collect(child, box);
    }

    private GeoUtil() {}
}