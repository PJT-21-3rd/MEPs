package collectors.hjdbriefing;

import java.sql.*;
import java.util.*;

public class HjdBriefingExtractor {
    private static final String FLPOP_MAX_YYQU_SQL = "SELECT MAX(yyqu) FROM hjd_flpop";
    private static final String STORE_MAX_YYQU_SQL = "SELECT MAX(yyqu) FROM hjd_store";
    private static final String FLPOP_PREV_YYQU_SQL = "SELECT MAX(yyqu) FROM hjd_flpop WHERE yyqu < ?";

    // 특정 분기의 hjd_flpop 총유동인구
    private static final String FLPOP_TOTAL_BY_YYQU_SQL =
            "SELECT hjd_cd, tot_flpop FROM hjd_flpop WHERE yyqu = ?";

    // 특정 분기의 연령대별 유동인구
    private static final String FLPOP_BY_YYQU_SQL =
            "SELECT hjd_cd, tot_flpop, agrde_10, agrde_20, agrde_30, agrde_40, agrde_50, agrde_60 " +
                    "FROM hjd_flpop " +
                    "WHERE yyqu = ?";

    // 특정 분기의 hjd_store 1위 업종
    private static final String TOP_STORE_BY_YYQU_SQL =
            "WITH ranked AS ( " +
                    "    SELECT hjd_cd, induty_nm, stor_co, " +
                    "           ROW_NUMBER() OVER (PARTITION BY hjd_cd ORDER BY stor_co DESC) AS rn " +
                    "    FROM hjd_store " +
                    "    WHERE yyqu = ? AND stor_co > 0 " +
                    ") " +
                    "SELECT hjd_cd, induty_nm AS top_induty_nm, stor_co AS top_induty_stor_cnt " +
                    "FROM ranked " +
                    "WHERE rn = 1";

    /** hjd_flpop 원본 1행 (계산 전 raw 값) */
    public record FlpopRow(
            String hjdCd, int totFlpop,
            int agrde10, int agrde20, int agrde30, int agrde40, int agrde50, int agrde60
    ) {
    }

    /** hjd_store 기준 1위 업종 */
    public record StoreStat(String topIndutyNm, Integer topIndutyStorCnt) {
    }

    /** hjd_flpop/hjd_store 중 더 이전 분기를 기준 분기로 선택 */
    public String resolveTargetYyqu(Connection conn) throws SQLException {
        String flpopMaxYyqu = queryScalarString(conn, FLPOP_MAX_YYQU_SQL);
        String storeMaxYyqu = queryScalarString(conn, STORE_MAX_YYQU_SQL);
        if (flpopMaxYyqu == null || storeMaxYyqu == null) {
            throw new IllegalStateException("hjd_flpop 또는 hjd_store에 데이터가 없습니다.");
        }

        String targetYyqu = flpopMaxYyqu.compareTo(storeMaxYyqu) <= 0 ? flpopMaxYyqu : storeMaxYyqu;
        return targetYyqu;
    }

    /** targetYyqu보다 이전 분기 중 가장 최신 분기 (없으면 null) */
    public String resolvePrevYyqu(Connection conn, String targetYyqu) throws SQLException {
        return queryScalarString(conn, FLPOP_PREV_YYQU_SQL, targetYyqu);
    }

    public List<FlpopRow> fetchFlpopRows(Connection conn, String yyqu) throws SQLException {
        List<FlpopRow> result = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(FLPOP_BY_YYQU_SQL)) {
            stmt.setString(1, yyqu);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new FlpopRow(
                            rs.getString("hjd_cd"),
                            rs.getInt("tot_flpop"),
                            getIntOrZero(rs, "agrde_10"), getIntOrZero(rs, "agrde_20"),
                            getIntOrZero(rs, "agrde_30"), getIntOrZero(rs, "agrde_40"),
                            getIntOrZero(rs, "agrde_50"), getIntOrZero(rs, "agrde_60")
                    ));
                }
            }
        }
        return result;
    }

    /** 특정 분기의 hjd_cd별 총 유동인구 */
    public Map<String, Integer> fetchTotFlpop(Connection conn, String yyqu) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        if (yyqu == null) return result;

        try (PreparedStatement stmt = conn.prepareStatement(FLPOP_TOTAL_BY_YYQU_SQL)) {
            stmt.setString(1, yyqu);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("hjd_cd"), rs.getInt("tot_flpop"));
                }
            }
        }
        return result;
    }

    public Map<String, StoreStat> fetchTopStore(Connection conn, String yyqu) throws SQLException {
        Map<String, StoreStat> result = new HashMap<>();
        try (PreparedStatement stmt = conn.prepareStatement(TOP_STORE_BY_YYQU_SQL)) {
            stmt.setString(1, yyqu);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("hjd_cd"), new StoreStat(
                            rs.getString("top_induty_nm"),
                            getNullableInt(rs, "top_induty_stor_cnt")
                    ));
                }
            }
        }
        return result;
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
