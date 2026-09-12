package collectors.fire;

import collectors.common.Db;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

/**
 * fire_station 테이블 적재 (일회성 배치)
 * 입력: data/소방청_전국소방서_좌표현황XY좌표_20240901.csv (CP949, 헤더 1행)
 * - 실측 결과 좌표는 이미 WGS84 십진수 → 좌표계 변환 없음
 *   ※ 단, 이 파일은 X=위도 / Y=경도이므로 POINT(경도, 위도) 순서로 스왑해 적재
 * - 주소가 '서울'로 시작하는 행만 적재 (소방서 24 + 119안전센터 113 = 137건)
 * - 자연키가 없어 DELETE → INSERT 전체 재생성으로 멱등성 확보
 */
public class FireStationLoader {

    public static void main(String[] args) throws Exception {
        List<String> lines = Files.readAllLines(
                Path.of("data", "fire_station(XY)_20240901.csv"),
                Charset.forName("MS949"));

        String sql = """
                INSERT INTO fire_station (station_nm, geom)
                VALUES (?, ST_SRID(POINT(?, ?), 4326))
                """;

        try (Connection conn = Db.connect()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                st.executeUpdate("DELETE FROM fire_station");

                int count = 0;
                for (String line : lines.subList(1, lines.size())) {   // 헤더 제외
                    // 필드 내 콤마·따옴표 없음 검증 완료 → 단순 split 안전
                    String[] c = line.split(",");
                    // c[1]=이름, c[2]=주소, c[5]=X(위도), c[6]=Y(경도), c[7]=유형
                    if (!c[2].startsWith("서울")) continue;
                    // 소방서만 적재하려면: if (!"소방서".equals(c[7])) continue;

                    double lat = Double.parseDouble(c[5]);
                    double lng = Double.parseDouble(c[6]);
                    if (lat < 37.4 || lat > 37.7 || lng < 126.7 || lng > 127.2) {
                        System.out.printf("서울 범위 밖 좌표 스킵: %s (%f, %f)%n", c[1], lat, lng);
                        continue;
                    }

                    ps.setString(1, c[1]);
                    ps.setDouble(2, lng);   // POINT(경도, 위도) — CSV의 Y가 경도
                    ps.setDouble(3, lat);
                    ps.addBatch();
                    count++;
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