package collectors.buildings;

import collectors.common.Config;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class BuildingDao {

    private static final int BATCH_SIZE = 100;

    private static final String SQL =
            "INSERT INTO buildings (" +
                    "  bd_mgt_sn, bjd_cd, hjd_cd, road_addr, jibun_addr, pnu," +
                    "  main_purps, bld_nm, grnd_flr, ugrnd_flr," +
                    "  footprint, center," +
                    "  use_apr_day, ho_cnt, viol_bd_yn, strct_cd_nm," +
                    "  lndcgr_code_nm, prpos_area_nm, road_side_code_nm, pblntf_pclnd," +
                    "  floor_info, parcel_geom" +
                    ") VALUES (" +
                    "  ?, ?," +
                    // hjd_cd — 건물 중심점을 포함하는 행정동 코드
                    "  (SELECT h.hjd_cd FROM hjd_boundary h" +
                    "    WHERE ST_Contains(h.geom, ST_GeomFromGeoJSON(?)) LIMIT 1)," +
                    "  ?, ?, ?," +
                    "  ?, ?, ?, ?," +
                    "  ST_GeomFromGeoJSON(?)," +
                    // center — 자바에서 계산한 중심점 (MySQL ST_Centroid는 SRID 4326 미지원)
                    "  ST_GeomFromGeoJSON(?)," +
                    "  ?, ?, ?, ?," +
                    "  ?, ?, ?, ?," +
                    "  ?," +
                    // parcel_geom — 지적도 없는 건물은 NULL
                    "  CASE WHEN ? IS NULL THEN NULL ELSE ST_GeomFromGeoJSON(?) END" +
                    ") ON DUPLICATE KEY UPDATE" +
                    "  bjd_cd = VALUES(bjd_cd)," +
                    "  hjd_cd = VALUES(hjd_cd)," +
                    "  road_addr = VALUES(road_addr)," +
                    "  jibun_addr = VALUES(jibun_addr)," +
                    "  pnu = VALUES(pnu)," +
                    "  main_purps = VALUES(main_purps)," +
                    "  bld_nm = VALUES(bld_nm)," +
                    "  grnd_flr = VALUES(grnd_flr)," +
                    "  ugrnd_flr = VALUES(ugrnd_flr)," +
                    "  footprint = VALUES(footprint)," +
                    "  center = VALUES(center)," +
                    "  use_apr_day = VALUES(use_apr_day)," +
                    "  ho_cnt = VALUES(ho_cnt)," +
                    "  viol_bd_yn = VALUES(viol_bd_yn)," +
                    "  strct_cd_nm = VALUES(strct_cd_nm)," +
                    "  lndcgr_code_nm = VALUES(lndcgr_code_nm)," +
                    "  prpos_area_nm = VALUES(prpos_area_nm)," +
                    "  road_side_code_nm = VALUES(road_side_code_nm)," +
                    "  pblntf_pclnd = VALUES(pblntf_pclnd)," +
                    "  floor_info = VALUES(floor_info)," +
                    "  parcel_geom = VALUES(parcel_geom)";

    private static Connection conn;
    private static PreparedStatement pstmt;
    private static int pending = 0;
    private static int totalInserted = 0;

    public static void open() {
        try {
            conn = DriverManager.getConnection(
                    Config.get("db.url"), Config.get("db.username"), Config.get("db.password"));
            conn.setAutoCommit(false);
            pstmt = conn.prepareStatement(SQL);
        } catch (SQLException e) {
            throw new RuntimeException("DB 연결 실패: " + e.getMessage(), e);
        }
    }

    public static Set<String> loadExistingKeys() {
        Set<String> keys = new HashSet<>();
        try (PreparedStatement st = conn.prepareStatement("SELECT bd_mgt_sn FROM buildings");
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) keys.add(rs.getString(1));
        } catch (SQLException e) {
            throw new RuntimeException("기적재 목록 조회 실패: " + e.getMessage(), e);
        }
        System.out.println("기적재 건물: " + keys.size() + "건 (재수집 시 건너뜀)");
        return keys;
    }

    public static void add(String bdMgtSn, String pnu, String footprintGeoJson,
                           String roadAddr, String jibunAddr,
                           String mainPurps, String bldNm, Integer grndFlr, Integer ugrndFlr,
                           String useAprDay, Integer hoCnt, String violBdYn, String strctCdNm,
                           String lndcgrCodeNm, String prposAreaNm, String roadSideCodeNm,
                           Long pblntfPclnd, String floorInfo, String parcelGeoJson) {

        if (footprintGeoJson == null) {
            throw new IllegalArgumentException("footprint는 필수입니다 (bd_mgt_sn=" + bdMgtSn + ")");
        }

        // center는 NOT NULL 컬럼 — 계산 실패 시 적재 불가
        String centerGeoJson = GeoUtil.centerOf(footprintGeoJson);
        if (centerGeoJson == null) {
            throw new IllegalArgumentException("중심점 계산 실패 (bd_mgt_sn=" + bdMgtSn + ")");
        }

        try {
            int i = 1;
            pstmt.setString(i++, bdMgtSn);
            pstmt.setString(i++, pnu.substring(0, 10));   // bjd_cd — pnu 앞 10자리
            pstmt.setString(i++, centerGeoJson);          // hjd_cd 서브쿼리용 (중심점 포함 판정)
            pstmt.setString(i++, roadAddr);
            pstmt.setString(i++, jibunAddr);
            pstmt.setString(i++, pnu);
            pstmt.setString(i++, mainPurps);
            pstmt.setString(i++, bldNm);
            setInt(pstmt, i++, grndFlr);
            setInt(pstmt, i++, ugrndFlr);
            pstmt.setString(i++, footprintGeoJson);       // footprint
            pstmt.setString(i++, centerGeoJson);          // center
            pstmt.setString(i++, useAprDay);
            setInt(pstmt, i++, hoCnt);
            pstmt.setString(i++, violBdYn);
            pstmt.setString(i++, strctCdNm);
            pstmt.setString(i++, lndcgrCodeNm);
            pstmt.setString(i++, prposAreaNm);
            pstmt.setString(i++, roadSideCodeNm);
            setLong(pstmt, i++, pblntfPclnd);
            pstmt.setString(i++, floorInfo);
            pstmt.setString(i++, parcelGeoJson);          // CASE WHEN 판정용
            pstmt.setString(i++, parcelGeoJson);          // ST_GeomFromGeoJSON용

            pstmt.addBatch();
            if (++pending >= BATCH_SIZE) flush();

        } catch (SQLException e) {
            throw new RuntimeException("배치 추가 실패 (bd_mgt_sn=" + bdMgtSn + "): "
                    + e.getMessage(), e);
        }
    }

    public static void flush() {
        if (pending == 0) return;
        try {
            int[] results = pstmt.executeBatch();
            conn.commit();
            totalInserted += results.length;
            pending = 0;
        } catch (SQLException e) {
            rollback();
            throw new RuntimeException("배치 실행 실패: " + e.getMessage(), e);
        }
    }

    public static void close() {
        try {
            flush();
            System.out.println("DB 적재 완료: " + totalInserted + "건");
        } finally {
            closeQuietly();
        }
    }

    private static void rollback() {
        try {
            if (conn != null) conn.rollback();
        } catch (SQLException ignore) {
        }
    }

    private static void closeQuietly() {
        try {
            if (pstmt != null) pstmt.close();
        } catch (SQLException ignore) {
        }
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignore) {
        }
        pstmt = null;
        conn = null;
    }

    private static void setInt(PreparedStatement pstmt, int idx, Integer v) throws SQLException {
        if (v == null) pstmt.setNull(idx, java.sql.Types.INTEGER);
        else pstmt.setInt(idx, v);
    }

    private static void setLong(PreparedStatement pstmt, int idx, Long v) throws SQLException {
        if (v == null) pstmt.setNull(idx, java.sql.Types.BIGINT);
        else pstmt.setLong(idx, v);
    }

    private BuildingDao() {}
}