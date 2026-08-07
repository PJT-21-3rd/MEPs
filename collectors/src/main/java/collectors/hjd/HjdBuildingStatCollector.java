package collectors.hjd;

import collectors.common.Db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class HjdBuildingStatCollector {
    private static final String AGGREGATE_AND_UPSERT_SQL =
            "INSERT INTO hjd_stat (hjd_cd, avg_bld_age, bld_cnt, computed_at) " +
                    "SELECT " +
                    "    hjd_cd, " +
                    "    ROUND(AVG(bld_age), 1) AS avg_bld_age, " +
                    "    COUNT(*) AS bld_cnt, " +
                    "    CURDATE() " +
                    "FROM ( " +
                    "    SELECT " +
                    "        b.hjd_cd, " +
                    "        CASE " +
                    "            WHEN LEFT(b.use_apr_day, 4) REGEXP '^[0-9]{4}$' " +
                    "                 AND LEFT(b.use_apr_day, 4) BETWEEN 1900 AND YEAR(CURDATE()) " +
                    "            THEN YEAR(CURDATE()) - CAST(LEFT(b.use_apr_day, 4) AS UNSIGNED) " +
                    "            ELSE NULL " +
                    "        END AS bld_age " +
                    "    FROM buildings b " +
                    "    INNER JOIN hjd h ON h.hjd_cd = b.hjd_cd " +
                    ") t " +
                    "GROUP BY hjd_cd " +
                    "HAVING AVG(bld_age) IS NOT NULL " +
                    "ON DUPLICATE KEY UPDATE " +
                    "    avg_bld_age = VALUES(avg_bld_age), " +
                    "    bld_cnt = VALUES(bld_cnt), " +
                    "    computed_at = VALUES(computed_at)";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        try (Connection conn = Db.connect();
             Statement stmt = conn.createStatement()) {

            int affectedRows = stmt.executeUpdate(AGGREGATE_AND_UPSERT_SQL);

            long endTime = System.currentTimeMillis();
            System.out.println("=========================================");
            System.out.printf("행정동 평균 건물 노후도 집계 완료 (소요시간: %.2f초)%n",
                    (endTime - startTime) / 1000.0);
            System.out.println("=========================================");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}