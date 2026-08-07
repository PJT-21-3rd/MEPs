package collectors.flood;

import collectors.common.Config;
import collectors.common.Db;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FloodTraceCollector {

    private static final String API_URL = Config.get("safetydata.floodApiUrl");
    private static final String SERVICE_KEY = Config.get("safetydata.serviceKey");

    private static final int PAGE_SIZE = 100;
    private static final int BATCH_SIZE = 500;

    private static final String SEOUL_CTPV_CD = "11";

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        String insertSql = "INSERT IGNORE INTO flood (sn, sgg_cd, grade, year, geom, cause) " +
                "VALUES (?, ?, ?, ?, ST_GeomFromText(?, 4326, 'axis-order=long-lat'), ?)";

        int totalInserted = 0;
        int totalSkipped = 0;
        int batchCount = 0;

        try (Connection conn = Db.connect();
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            conn.setAutoCommit(false);

            int pageNo = 1;
            int totalCount = Integer.MAX_VALUE;

            while ((pageNo - 1) * PAGE_SIZE < totalCount) {
                JsonNode root = callApi(pageNo);

                JsonNode header = root.path("header");
                if (!"00".equals(header.path("resultCode").asText())) {
                    System.err.println("API 오류: " + header.path("errorMsg").asText(header.toString()));
                    break;
                }

                totalCount = root.path("totalCount").asInt(totalCount);
                JsonNode body = root.path("body");
                if (!body.isArray() || body.isEmpty()) break;

                for (JsonNode item : body) {
                    String ctpvCd = item.path("STDG_CTPV_CD").asText(null);
                    if (!SEOUL_CTPV_CD.equals(ctpvCd)) {
                        totalSkipped++;
                        continue;
                    }

                    long sn = item.path("SN").asLong();
                    String sggCd = item.path("STDG_SGG_CD").asText(null);
                    int grade = item.path("FLDN_GRD").asInt();
                    String year = item.path("FLDN_YR").asText(null);
                    String rawGeom = item.path("GEOM").asText(null);
                    String wkt = buildMultiPolygonWkt(rawGeom);
                    String cause = item.path("FLDN_DST_NM").asText("").trim();
                    if (cause.isEmpty()) cause = null;

                    if (sggCd == null || year == null || wkt == null) {
                        totalSkipped++;
                        continue;
                    }

//                    int yr = Integer.parseInt(year.trim());
//                    if (yr < 2020) {
//                        totalSkipped++;
//                        continue;
//                    }

                    pstmt.setLong(1, sn);
                    pstmt.setString(2, sggCd);
                    pstmt.setInt(3, grade);
                    pstmt.setString(4, year);
                    pstmt.setString(5, wkt);
                    pstmt.setString(6, cause);

                    pstmt.addBatch();
                    batchCount++;
                    totalInserted++;

                    if (batchCount % BATCH_SIZE == 0) {
                        pstmt.executeBatch();
                        conn.commit();
                    }
                }

                pageNo++;
            }

            if (batchCount % BATCH_SIZE != 0) {
                pstmt.executeBatch();
                conn.commit();
            }

            long endTime = System.currentTimeMillis();
            System.out.println("=========================================");
            System.out.printf("저장된 데이터: %d건 (스킵: %d건, 소요시간: %.2f초)%n",
                    totalInserted, totalSkipped, (endTime - startTime) / 1000.0);
            System.out.println("=========================================");

        } catch (SQLException e) {
            //e.printStackTrace();
            while (e != null) {
                System.out.println("SQLState : " + e.getSQLState());
                System.out.println("ErrorCode: " + e.getErrorCode());
                System.out.println("Message  : " + e.getMessage());
                e = e.getNextException();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonNode callApi(int pageNo) throws Exception {
        String url = API_URL
                + "?serviceKey=" + URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8)
                + "&pageNo=" + pageNo
                + "&numOfRows=" + PAGE_SIZE
                + "&returnType=json";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP 오류: " + response.statusCode() + " body=" + response.body());
        }

        return mapper.readTree(response.body());
    }


    private static final Pattern COORD_PAIR =
            Pattern.compile("(-?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?)\\s+(-?\\d+(?:\\.\\d+)?(?:[eE][-+]?\\d+)?)");


    private static String buildMultiPolygonWkt(String rawGeom) {
        if (rawGeom == null || rawGeom.isBlank()) return null;
        String trimmed = rawGeom.trim();

        String body;
        boolean alreadyMulti;
        if (trimmed.startsWith("MULTIPOLYGON")) {
            body = trimmed.substring("MULTIPOLYGON".length()).trim();
            alreadyMulti = true;
        } else if (trimmed.startsWith("POLYGON")) {
            body = trimmed.substring("POLYGON".length()).trim();
            alreadyMulti = false;
        } else {
            return null;
        }

        String convertedBody = convertCoordinates(body);

        return alreadyMulti
                ? "MULTIPOLYGON" + convertedBody
                : "MULTIPOLYGON(" + convertedBody + ")";
    }

    private static String convertCoordinates(String wktBody) {
        Matcher m = COORD_PAIR.matcher(wktBody);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            double x = Double.parseDouble(m.group(1));
            double y = Double.parseDouble(m.group(2));
            double[] lonLat = toWgs84(x, y);
            m.appendReplacement(sb, Matcher.quoteReplacement(lonLat[0] + " " + lonLat[1]));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**좌표계 변환
     * EPSG:3857(Web Mercator) -> EPSG:4326(WGS84)*/
    private static double[] toWgs84(double x, double y) {
        final double R = 6378137.0;
        double lon = x / R * (180.0 / Math.PI);
        double lat = (Math.PI / 2 - 2 * Math.atan(Math.exp(-y / R))) * (180.0 / Math.PI);
        return new double[]{lon, lat};
    }
}