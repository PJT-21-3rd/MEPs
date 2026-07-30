package collectors.sinkhole;

import collectors.common.Db;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * 5단계: sinkhole 테이블 적재
 * 입력: data/sinkhole_geocoded_pnu_filled.json (158건, PNU 전건 보유)
 * - ON DUPLICATE KEY UPDATE 로 재실행 멱등성 확보 (PK = sago_no)
 * - geom: POINT(경도 위도) + SRID 4326, axis-order=long-lat 명시
 */
public class SinkholeLoader {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        JsonNode list = mapper.readTree(
                Files.readString(Path.of("data", "sinkhole_geocoded_pnu_filled.json")));

        String sql = """
                INSERT INTO sinkhole (sago_no, sgg_nm, bjd_nm, sago_date, pnu, geom)
                VALUES (?, ?, ?, ?, ?, ST_GeomFromText(?, 4326, 'axis-order=long-lat'))
                ON DUPLICATE KEY UPDATE
                    sgg_nm = VALUES(sgg_nm),
                    bjd_nm = VALUES(bjd_nm),
                    sago_date = VALUES(sago_date),
                    pnu = VALUES(pnu),
                    geom = VALUES(geom)
                """;

        try (Connection conn = Db.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int count = 0;
                for (JsonNode item : list) {
                    ps.setString(1, item.path("sago_no").asText());
                    ps.setString(2, item.path("sgg_nm").asText());
                    ps.setString(3, item.path("bjd_nm").asText());
                    ps.setString(4, item.path("sago_date").asText());   // CHAR(8)
                    ps.setString(5, item.path("pnu").asText());
                    ps.setString(6, "POINT(%s %s)".formatted(
                            item.path("lon").asDouble(), item.path("lat").asDouble()));

                    ps.addBatch();
                    if (++count % 50 == 0) ps.executeBatch();
                }
                ps.executeBatch();
                conn.commit();
                System.out.printf("적재 완료: %d건%n", count);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
}