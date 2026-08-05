package collectors.sinkhole;

import collectors.common.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 6단계: building_sinkhole_map 적재 (건물-지반침하사고 매핑 사전 계산)
 * - 기준: 건물 center 반경 500m 이내 사고 (ST_Distance_Sphere)
 * - 순수 파생 테이블이므로 전량 삭제 후 재생성으로 멱등성 확보
 * - 사고 지점별 bbox를 MBRContains로 걸어 buildings.center의
 *   공간 인덱스(idx_center)를 태운 뒤 거리 조건으로 정밀 판정
 * - bbox WKT POLYGON은 위도-우선 (backend BuildingMapper.xml과 동일 컨벤션)
 */
public class BuildingSinkholeMapLoader {

    private static final double RADIUS_M = 500;
    // 서울 위도 기준 500m를 여유 있게 덮는 위경도 폭 (최종 판정은 거리 조건이 수행)
    private static final double LAT_DELTA = 0.0046;
    private static final double LNG_DELTA = 0.0058;

    private record Sago(String sagoNo, double lat, double lng) {}

    public static void main(String[] args) throws Exception {
        String insertSql = """
                INSERT INTO building_sinkhole_map (bd_mgt_sn, sago_no)
                SELECT b.bd_mgt_sn, s.sago_no
                FROM sinkhole s
                JOIN buildings b
                  ON MBRContains(ST_GeomFromText(?, 4326), b.center)
                WHERE s.sago_no = ?
                  AND ST_Distance_Sphere(b.center, s.geom) <= ?
                """;

        try (Connection conn = Db.connect()) {
            conn.setAutoCommit(false);

            try {
                int deleted;
                try (Statement st = conn.createStatement()) {
                    deleted = st.executeUpdate("DELETE FROM building_sinkhole_map");
                }

                List<Sago> sagos = new ArrayList<>();
                try (Statement st = conn.createStatement();
                     ResultSet rs = st.executeQuery(
                             "SELECT sago_no, ST_Latitude(geom), ST_Longitude(geom) FROM sinkhole")) {
                    while (rs.next()) {
                        sagos.add(new Sago(rs.getString(1), rs.getDouble(2), rs.getDouble(3)));
                    }
                }

                int total = 0;
                int matchedSago = 0;
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (Sago sago : sagos) {
                        ps.setString(1, bboxWkt(sago.lat(), sago.lng()));
                        ps.setString(2, sago.sagoNo());
                        ps.setDouble(3, RADIUS_M);
                        int inserted = ps.executeUpdate();
                        if (inserted > 0) matchedSago++;
                        total += inserted;
                    }
                }

                conn.commit();
                System.out.printf("기존 %d건 삭제, 사고 %d건 중 %d건 매칭, 매핑 %d건 적재 완료%n",
                        deleted, sagos.size(), matchedSago, total);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /** 사고 지점을 중심으로 한 bbox를 위도-우선 WKT POLYGON으로 조립 */
    private static String bboxWkt(double lat, double lng) {
        double south = lat - LAT_DELTA;
        double north = lat + LAT_DELTA;
        double west = lng - LNG_DELTA;
        double east = lng + LNG_DELTA;
        return "POLYGON((" + south + " " + west + "," + south + " " + east + ","
                + north + " " + east + "," + north + " " + west + "," + south + " " + west + "))";
    }
}
