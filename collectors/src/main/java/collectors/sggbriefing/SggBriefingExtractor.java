package collectors.sggbriefing;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class SggBriefingExtractor {

    private static final String ALL_SGG_CD_SQL = "SELECT sgg_cd FROM sgg";

    private static final String FLPOP_MAX_YYQU_SQL = "SELECT MAX(yyqu) FROM hjd_flpop";
    private static final String STORE_MAX_YYQU_SQL = "SELECT MAX(yyqu) FROM hjd_store";
    private static final String FLPOP_PREV_YYQU_SQL = "SELECT MAX(yyqu) FROM hjd_flpop WHERE yyqu < ?";

    // 특정 구·분기의 연령대별 합산 유동인구
    private static final String FLPOP_BY_SGG_YYQU_SQL =
            "SELECT SUM(f.tot_flpop) AS tot_flpop, " +
                    "       SUM(f.agrde_10) AS agrde_10, SUM(f.agrde_20) AS agrde_20, " +
                    "       SUM(f.agrde_30) AS agrde_30, SUM(f.agrde_40) AS agrde_40, " +
                    "       SUM(f.agrde_50) AS agrde_50, SUM(f.agrde_60) AS agrde_60 " +
                    "FROM hjd_flpop f " +
                    "JOIN hjd h ON h.hjd_cd = f.hjd_cd " +
                    "WHERE h.sgg_cd = ? AND f.yyqu = ?";

    // 특정 구·분기의 총 유동인구
    private static final String TOTAL_FLPOP_BY_SGG_YYQU_SQL =
            "SELECT SUM(f.tot_flpop) AS tot_flpop " +
                    "FROM hjd_flpop f " +
                    "JOIN hjd h ON h.hjd_cd = f.hjd_cd " +
                    "WHERE h.sgg_cd = ? AND f.yyqu = ?";

    // 특정 구·분기의 1위 업종
    private static final String TOP_STORE_BY_SGG_YYQU_SQL =
            "SELECT s.induty_nm, SUM(s.stor_co) AS stor_co " +
                    "FROM hjd_store s " +
                    "JOIN hjd h ON h.hjd_cd = s.hjd_cd " +
                    "WHERE h.sgg_cd = ? AND s.yyqu = ? AND s.stor_co > 0 " +
                    "GROUP BY s.induty_nm " +
                    "ORDER BY stor_co DESC " +
                    "LIMIT 1";

    // 구에 속한 행정동별 평균 건물연령·동수
    private static final String HJD_STAT_BY_SGG_SQL =
            "SELECT st.avg_bld_age, st.bld_cnt " +
                    "FROM hjd_stat st " +
                    "JOIN hjd h ON h.hjd_cd = st.hjd_cd " +
                    "WHERE h.sgg_cd = ?";

    private static final String EXISTING_SGG_STAT_SQL =
            "SELECT daily_flpop, flpop_chg_rate, top_induty_nm, top_induty_stor_cnt, " +
                    "       major_age_grp, major_age_ratio " +
                    "FROM sgg_ai_briefing WHERE sgg_cd = ?";


    public record SggFlpopTotal(
            int totFlpop,
            int agrde10, int agrde20, int agrde30, int agrde40, int agrde50, int agrde60
    ) {
    }

    public record TopStore(String indutyNm, int storCnt) {
    }

    public record HjdStatRow(BigDecimal avgBldAge, int bldCnt) {
    }

    public List<String> fetchAllSggCds(Connection conn) throws SQLException {
        List<String> result = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(ALL_SGG_CD_SQL)) {
            while (rs.next()) {
                result.add(rs.getString("sgg_cd"));
            }
        }
        return result;
    }

    /** hjd_flpop/hjd_store 중 더 이전 분기를 기준 분기로 선택 */
    public String resolveTargetYyqu(Connection conn) throws SQLException {
        String flpopMaxYyqu = queryScalarString(conn, FLPOP_MAX_YYQU_SQL);
        String storeMaxYyqu = queryScalarString(conn, STORE_MAX_YYQU_SQL);
        if (flpopMaxYyqu == null || storeMaxYyqu == null) {
            throw new IllegalStateException("hjd_flpop 또는 hjd_store에 데이터가 없습니다.");
        }
        return flpopMaxYyqu.compareTo(storeMaxYyqu) <= 0 ? flpopMaxYyqu : storeMaxYyqu;
    }

    public String resolvePrevYyqu(Connection conn, String targetYyqu) throws SQLException {
        return queryScalarString(conn, FLPOP_PREV_YYQU_SQL, targetYyqu);
    }

    /** 특정 구·분기의 연령대별 합산 유동인구. 데이터 없으면 null */
    public SggFlpopTotal fetchFlpopTotal(Connection conn, String sggCd, String yyqu) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(FLPOP_BY_SGG_YYQU_SQL)) {
            stmt.setString(1, sggCd);
            stmt.setString(2, yyqu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getObject("tot_flpop") == null) {
                    return null;
                }
                return new SggFlpopTotal(
                        rs.getInt("tot_flpop"),
                        getIntOrZero(rs, "agrde_10"), getIntOrZero(rs, "agrde_20"),
                        getIntOrZero(rs, "agrde_30"), getIntOrZero(rs, "agrde_40"),
                        getIntOrZero(rs, "agrde_50"), getIntOrZero(rs, "agrde_60")
                );
            }
        }
    }

    /** 특정 구·분기의 총 유동인구(직전 분기 대비용). 데이터 없거나 yyqu가 null이면 null */
    public Integer fetchTotalFlpop(Connection conn, String sggCd, String yyqu) throws SQLException {
        if (yyqu == null) {
            return null;
        }
        try (PreparedStatement stmt = conn.prepareStatement(TOTAL_FLPOP_BY_SGG_YYQU_SQL)) {
            stmt.setString(1, sggCd);
            stmt.setString(2, yyqu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next() || rs.getObject("tot_flpop") == null) {
                    return null;
                }
                return rs.getInt("tot_flpop");
            }
        }
    }

    /** 특정 구·분기의 1위 업종(구 전체 점포수 합산 기준). 데이터 없으면 null */
    public TopStore fetchTopStore(Connection conn, String sggCd, String yyqu) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(TOP_STORE_BY_SGG_YYQU_SQL)) {
            stmt.setString(1, sggCd);
            stmt.setString(2, yyqu);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new TopStore(rs.getString("induty_nm"), rs.getInt("stor_co"));
            }
        }
    }

    /** 구에 속한 행정동별 평균 건물연령·동수 원자료 */
    public List<HjdStatRow> fetchHjdStats(Connection conn, String sggCd) throws SQLException {
        List<HjdStatRow> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(HJD_STAT_BY_SGG_SQL)) {
            stmt.setString(1, sggCd);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new HjdStatRow(rs.getBigDecimal("avg_bld_age"), rs.getInt("bld_cnt")));
                }
            }
        }
        return result;
    }

    /** sgg_ai_briefing에 이미 저장된 통계 스냅샷 (변경 감지용). 없으면 null */
    public SggBriefingLoader.Stat fetchExistingStat(Connection conn, String sggCd) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(EXISTING_SGG_STAT_SQL)) {
            stmt.setString(1, sggCd);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new SggBriefingLoader.Stat(
                        getNullableInt(rs, "daily_flpop"),
                        rs.getBigDecimal("flpop_chg_rate"),
                        rs.getString("top_induty_nm"),
                        getNullableInt(rs, "top_induty_stor_cnt"),
                        rs.getString("major_age_grp"),
                        rs.getBigDecimal("major_age_ratio")
                );
            }
        }
    }

    private static int getIntOrZero(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? 0 : value;
    }

    private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static String queryScalarString(Connection conn, String sql, String... params) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setString(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }
}
