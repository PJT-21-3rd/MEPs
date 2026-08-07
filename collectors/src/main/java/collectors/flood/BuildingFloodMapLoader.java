package collectors.flood;

import collectors.common.Db;

import java.sql.*;

public class BuildingFloodMapLoader {

    private static final String DELETE_SQL = "DELETE FROM building_flood_map";

    private static final String COUNT_MATCHED_FLOOD_SQL = """
            SELECT COUNT(DISTINCT f.sn)
            FROM flood f
            JOIN buildings b
              ON b.bjd_cd LIKE CONCAT(f.sgg_cd, '%')
             AND ST_Intersects(f.geom, b.footprint)
            """;

    private static final String INSERT_SQL = """
            INSERT INTO building_flood_map (bd_mgt_sn, sn)
            SELECT b.bd_mgt_sn, f.sn
            FROM flood f
            JOIN buildings b
              ON b.bjd_cd LIKE CONCAT(f.sgg_cd, '%')
             AND ST_Intersects(f.geom, b.footprint)
            """;

    public static void main(String[] args) throws Exception {

        try (Connection conn = Db.connect()) {
            conn.setAutoCommit(false);
            try {
                int deleted = deleteExisting(conn);
                int matchedFlood = countMatchedFlood(conn);
                int inserted = insertMappings(conn);

                conn.commit();

                System.out.printf(
                        "기존 %d건 삭제, 침수흔적 %d건 매칭, 매핑 %d건 적재 완료",
                        deleted, matchedFlood, inserted);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private static int deleteExisting(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            return st.executeUpdate(DELETE_SQL);
        }
    }

    private static int countMatchedFlood(Connection conn) throws Exception {
        try (Statement st = conn.createStatement();
             var rs = st.executeQuery(COUNT_MATCHED_FLOOD_SQL)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static int insertMappings(Connection conn) throws Exception {
        try (Statement st = conn.createStatement()) {
            return st.executeUpdate(INSERT_SQL);
        }
    }
}
