package collectors.hjdbriefing;

import java.math.BigDecimal;
import java.util.*;
import java.sql.*;

/**
 * hjd_ai_briefing의 기존 통계값을 읽어 변경 여부를 판단하고, 새 통계값을 UPSERT한다.
 */
public class HjdBriefingLoader {

    private static final String SELECT_EXISTING_SQL =
            "SELECT hjd_cd, daily_flpop, flpop_chg_rate, top_induty_nm, top_induty_stor_cnt, " +
                    "       major_age_grp, major_age_ratio " +
                    "FROM hjd_ai_briefing";

    // ai_brf/ai_model_nm은 데이터가 변경되었을 때만 NULL로 리셋
    private static final String UPSERT_SQL =
            "INSERT INTO hjd_ai_briefing " +
                    "    (hjd_cd, daily_flpop, flpop_chg_rate, top_induty_nm, top_induty_stor_cnt, major_age_grp, major_age_ratio) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "    daily_flpop = VALUES(daily_flpop), " +
                    "    flpop_chg_rate = VALUES(flpop_chg_rate), " +
                    "    top_induty_nm = VALUES(top_induty_nm), " +
                    "    top_induty_stor_cnt = VALUES(top_induty_stor_cnt), " +
                    "    major_age_grp = VALUES(major_age_grp), " +
                    "    major_age_ratio = VALUES(major_age_ratio), " +
                    "    ai_brf = CASE WHEN ? THEN NULL ELSE ai_brf END, " +
                    "    ai_model_nm = CASE WHEN ? THEN NULL ELSE ai_model_nm END";

    /** hjd_ai_briefing에 저장될(혹은 이미 저장되어 있는) 통계 스냅샷 */
    public record Stat(
            Integer dailyFlpop,
            BigDecimal flpopChgRate,
            String topIndutyNm,
            Integer topIndutyStorCnt,
            String majorAgeGrp,
            BigDecimal majorAgeRatio
    ) {
        boolean differsFrom(Stat other) {
            if (other == null) return true;
            return !Objects.equals(dailyFlpop, other.dailyFlpop)
                    || !bigDecimalEquals(flpopChgRate, other.flpopChgRate)
                    || !Objects.equals(topIndutyNm, other.topIndutyNm)
                    || !Objects.equals(topIndutyStorCnt, other.topIndutyStorCnt)
                    || !Objects.equals(majorAgeGrp, other.majorAgeGrp)
                    || !bigDecimalEquals(majorAgeRatio, other.majorAgeRatio);
        }

        private static boolean bigDecimalEquals(BigDecimal a, BigDecimal b) {
            if (a == null || b == null) return a == b;
            return a.compareTo(b) == 0; // decimal(5,2) 컬럼이라도 스케일 차이 방어
        }
    }

    public record UpsertResult(int upsertCount, int resetCount) {
    }

    public Map<String, Stat> loadExisting(Connection conn) throws SQLException {
        Map<String, Stat> result = new HashMap<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_EXISTING_SQL)) {
            while (rs.next()) {
                result.put(rs.getString("hjd_cd"), new Stat(
                        getNullableInt(rs, "daily_flpop"),
                        rs.getBigDecimal("flpop_chg_rate"),
                        rs.getString("top_induty_nm"),
                        getNullableInt(rs, "top_induty_stor_cnt"),
                        rs.getString("major_age_grp"),
                        rs.getBigDecimal("major_age_ratio")
                ));
            }
        }
        return result;
    }

    /** existingStats와 비교해 값이 달라진 행만 ai_brf를 리셋하며 UPSERT한다. 커밋은 호출자 책임. */
    public UpsertResult upsertAll(Connection conn, Map<String, Stat> newStats, Map<String, Stat> existingStats)
            throws SQLException {
        int upsertCount = 0;
        int resetCount = 0;

        try (PreparedStatement stmt = conn.prepareStatement(UPSERT_SQL)) {
            for (Map.Entry<String, Stat> entry : newStats.entrySet()) {
                String hjdCd = entry.getKey();
                Stat stat = entry.getValue();
                boolean changed = stat.differsFrom(existingStats.get(hjdCd));
                if (changed) resetCount++;

                bindParams(stmt, hjdCd, stat, changed);
                stmt.addBatch();
                upsertCount++;
            }
            stmt.executeBatch();
        }

        return new UpsertResult(upsertCount, resetCount);
    }

    private void bindParams(PreparedStatement stmt, String hjdCd, Stat stat, boolean changed) throws SQLException {
        stmt.setString(1, hjdCd);
        setNullableInt(stmt, 2, stat.dailyFlpop());
        stmt.setBigDecimal(3, stat.flpopChgRate());
        stmt.setString(4, stat.topIndutyNm());
        setNullableInt(stmt, 5, stat.topIndutyStorCnt());
        stmt.setString(6, stat.majorAgeGrp());
        stmt.setBigDecimal(7, stat.majorAgeRatio());
        stmt.setBoolean(8, changed);
        stmt.setBoolean(9, changed);
    }

    private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }
}
