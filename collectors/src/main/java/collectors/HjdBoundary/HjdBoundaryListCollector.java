package collectors.HjdBoundary;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

public class HjdBoundaryListCollector {

    private static final String GEOJSON_PATH =
            "/Users/home/Desktop/HangJeongDong_ver20260701.geojson";

    private static final String BASE_VER = "20260701";

    private static final int LIMIT = 999999;

    private static final String SQL =
            "INSERT INTO hjd_boundary (hjd_cd, hjd_nm, sgg_nm, geom, base_ver) " +
                    "VALUES (?, ?, ?, ST_GeomFromGeoJSON(?, 1, 4326), ?)";

    public static void main(String[] args) {
        Properties prop = new Properties();

        try (InputStream input = HjdBoundaryListCollector.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {

            prop.load(input);
            String url  = prop.getProperty("db.url");
            String user = prop.getProperty("db.username");
            String pass = prop.getProperty("db.password");

            ObjectMapper om = new ObjectMapper();
            JsonNode features = om.readTree(new File(GEOJSON_PATH)).get("features");

            int count = 0;

            try (Connection conn = DriverManager.getConnection(url, user, pass);
                 PreparedStatement pstmt = conn.prepareStatement(SQL)) {

                for (JsonNode feature : features) {
                    JsonNode props = feature.get("properties");


                    if (!"11".equals(props.get("sido").asText())) continue;

                    String hjdCd = props.get("adm_cd2").asText().substring(0, 8);
                    String hjdNm = lastToken(props.get("adm_nm").asText());
                    String sggNm = props.get("sggnm").asText();
                    String geom  = feature.get("geometry").toString();

                    pstmt.setString(1, hjdCd);
                    pstmt.setString(2, hjdNm);
                    pstmt.setString(3, sggNm);
                    pstmt.setString(4, geom);
                    pstmt.setString(5, BASE_VER);
                    pstmt.executeUpdate();

                    count++;
                    System.out.println("적재: " + hjdCd + " | " + hjdNm + " | " + sggNm);

                    if (count >= LIMIT) break;
                }
            }
            System.out.println(" 적재 완료: " + count + "건");

        } catch (Exception e) {
            System.err.println(" 적재 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String lastToken(String admNm) {
        String[] parts = admNm.trim().split("\\s+");
        return parts[parts.length - 1];
    }
}