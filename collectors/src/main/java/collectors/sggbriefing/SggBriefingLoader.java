package collectors.sggbriefing;

import java.math.BigDecimal;
import java.sql.*;
import java.util.Objects;

public class SggBriefingLoader {

    private static final String UPSERT_SQL =
            "INSERT INTO sgg_ai_briefing " +
                    "    (sgg_cd, daily_flpop, flpop_chg_rate, top_induty_nm, top_induty_stor_cnt, total_induty_cnt, major_age_grp, major_age_ratio) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "    daily_flpop = VALUES(daily_flpop), " +
                    "    flpop_chg_rate = VALUES(flpop_chg_rate), " +
                    "    top_induty_nm = VALUES(top_induty_nm), " +
                    "    top_induty_stor_cnt = VALUES(top_induty_stor_cnt), " +
                    "    total_induty_cnt = VALUES(total_induty_cnt), " +
                    "    major_age_grp = VALUES(major_age_grp), " +
                    "    major_age_ratio = VALUES(major_age_ratio), " +
                    "    ai_brf = CASE WHEN ? THEN NULL ELSE ai_brf END, " +
                    "    ai_model_nm = CASE WHEN ? THEN NULL ELSE ai_model_nm END";


    public record Stat(
            Integer dailyFlpop,
            BigDecimal flpopChgRate,
            String topIndutyNm,
            Integer topIndutyStorCnt,
            Integer totalIndutyCnt,
            String majorAgeGrp,
            BigDecimal majorAgeRatio
    ) {
        boolean differsFrom(Stat other) {
            if (other == null) return true;
            return !Objects.equals(dailyFlpop, other.dailyFlpop)
                    || !bigDecimalEquals(flpopChgRate, other.flpopChgRate)
                    || !Objects.equals(topIndutyNm, other.topIndutyNm)
                    || !Objects.equals(topIndutyStorCnt, other.topIndutyStorCnt)
                    || !Objects.equals(totalIndutyCnt, other.totalIndutyCnt)
                    || !Objects.equals(majorAgeGrp, other.majorAgeGrp)
                    || !bigDecimalEquals(majorAgeRatio, other.majorAgeRatio);
        }

        private static boolean bigDecimalEquals(BigDecimal a, BigDecimal b) {
            if (a == null || b == null) return a == b;
            return a.compareTo(b) == 0;
        }
    }

    public boolean upsert(Connection conn, String sggCd, Stat stat, Stat existing) throws SQLException {
        boolean changed = stat.differsFrom(existing);
        try (PreparedStatement stmt = conn.prepareStatement(UPSERT_SQL)) {
            stmt.setString(1, sggCd);
            setNullableInt(stmt, 2, stat.dailyFlpop());
            stmt.setBigDecimal(3, stat.flpopChgRate());
            stmt.setString(4, stat.topIndutyNm());
            setNullableInt(stmt, 5, stat.topIndutyStorCnt());
            setNullableInt(stmt, 6, stat.totalIndutyCnt());
            stmt.setString(7, stat.majorAgeGrp());
            stmt.setBigDecimal(8, stat.majorAgeRatio());
            stmt.setBoolean(9, changed);
            stmt.setBoolean(10, changed);
            stmt.executeUpdate();
        }
        return changed;
    }

    private static void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setInt(index, value);
        }
    }
}
