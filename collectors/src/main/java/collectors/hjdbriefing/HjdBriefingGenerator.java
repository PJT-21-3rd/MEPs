package collectors.hjdbriefing;

import collectors.common.Db;
import collectors.common.OpenAiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.*;

public class HjdBriefingGenerator {

    private static final String SELECT_TARGETS_SQL =
            "SELECT b.hjd_cd, s.sgg_nm, h.hjd_nm, " +
                    "       b.daily_flpop, b.flpop_chg_rate, " +
                    "       b.top_induty_nm, b.top_induty_stor_cnt, " +
                    "       b.major_age_grp, b.major_age_ratio, " +
                    "       st.avg_bld_age, st.bld_cnt " +
                    "FROM hjd_ai_briefing b " +
                    "JOIN hjd h ON h.hjd_cd = b.hjd_cd " +
                    "JOIN sgg s ON s.sgg_cd = h.sgg_cd " +
                    "LEFT JOIN hjd_stat st ON st.hjd_cd = b.hjd_cd " +
                    "WHERE b.ai_brf IS NULL";

    private static final String UPDATE_BRIEFING_SQL =
            "UPDATE hjd_ai_briefing " +
                    "SET ai_brf = ?, ai_model_nm = ?, generated_at = NOW() " +
                    "WHERE hjd_cd = ?";

    private static final String SYSTEM_PROMPT =
            "당신은 서울시 상권/생활인구 데이터를 요약하는 애널리스트입니다. " +
                    "사용자 메시지는 한 행정동의 통계를 담은 JSON입니다. " +
                    "이를 바탕으로 3~4문장의 자연스러운 한국어 브리핑을 작성하세요. " +
                    "수치를 단순 나열하지 말고 의미 있는 특징 위주로 서술하세요. " +
                    "값이 null인 필드는 데이터가 없다는 뜻이니 언급하지 말고, 추측해서 채우지도 마세요.";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private record BriefingContext(
            String sggNm,
            String hjdNm,
            Integer dailyFlpop,
            BigDecimal flpopChgRatePercent,
            String topIndutyNm,
            Integer topIndutyStorCnt,
            String majorAgeGrp,
            BigDecimal majorAgeRatioPercent,
            BigDecimal avgBldAgeYears,
            Integer bldCnt
    ) {
    }

    public static void main(String[] args) {
        int successCount = 0;
        int failCount = 0;
        long startTime = System.currentTimeMillis();

        OpenAiClient openAiClient = new OpenAiClient();

        try (Connection conn = Db.connect();
             PreparedStatement selectStmt = conn.prepareStatement(SELECT_TARGETS_SQL);
             ResultSet rs = selectStmt.executeQuery()) {

            while (rs.next()) {
                String hjdCd = rs.getString("hjd_cd");

                try {
                    String userPrompt = buildUserPrompt(rs);
                    OpenAiClient.BriefingResult result = openAiClient.generateBriefing(SYSTEM_PROMPT, userPrompt);

                    updateBriefing(conn, hjdCd, result.text(), result.modelName());
                    successCount++;
                    System.out.printf("[OK] %s 브리핑 생성 완료%n", hjdCd);
                } catch (Exception e) {
                    failCount++;
                    System.err.printf("[FAIL] %s 브리핑 생성 실패: %s%n", hjdCd, e.getMessage());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("=========================================");
        System.out.printf("행정동 AI 브리핑 생성 완료 (성공: %d건, 실패: %d건, 소요시간: %.2f초)%n",
                successCount, failCount, (endTime - startTime) / 1000.0);
        System.out.println("=========================================");
    }

    private static String buildUserPrompt(ResultSet rs) throws SQLException, JsonProcessingException {
        BriefingContext context = new BriefingContext(
                rs.getString("sgg_nm"),
                rs.getString("hjd_nm"),
                getNullableInt(rs, "daily_flpop"),
                rs.getBigDecimal("flpop_chg_rate"),
                rs.getString("top_induty_nm"),
                getNullableInt(rs, "top_induty_stor_cnt"),
                rs.getString("major_age_grp"),
                rs.getBigDecimal("major_age_ratio"),
                rs.getBigDecimal("avg_bld_age"),
                getNullableInt(rs, "bld_cnt")
        );
        return OBJECT_MAPPER.writeValueAsString(context);
    }

    private static void updateBriefing(Connection conn, String hjdCd, String briefingText, String modelName)
            throws SQLException {
        try (PreparedStatement updateStmt = conn.prepareStatement(UPDATE_BRIEFING_SQL)) {
            updateStmt.setString(1, briefingText);
            updateStmt.setString(2, modelName);
            updateStmt.setString(3, hjdCd);
            updateStmt.executeUpdate();
        }
    }

    private static Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

}
