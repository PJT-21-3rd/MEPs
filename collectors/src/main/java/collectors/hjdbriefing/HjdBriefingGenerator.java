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

    private static final String SYSTEM_PROMPT = String.join("\n",
            "당신은 서울시 상권/생활인구 데이터를 요약하는 애널리스트입니다.",
            "사용자 메시지는 한 행정동(sggNm 구, hjdNm 동)의 통계를 담은 JSON입니다.",
            "이를 바탕으로 자연스러운 한국어 문장 4~5개로 브리핑을 작성하세요. 라벨이나 JSON 없이 문장만 출력합니다.",
            "",
            "[우선순위 — 아래 신호가 두드러지면 해당 내용을 가장 먼저 언급하세요]",
            "- flpopChgRatePercent 절댓값이 10 이상이면 유동인구 증감을 가장 먼저 언급 (양수=증가, 음수=감소)",
            "- majorAgeRatioPercent가 30 이상이면 특정 연령대 쏠림을 언급",
            "- avgBldAgeYears가 30 이상이면 건물 노후도를 언급",
            "- 위 조건에 해당하는 신호가 하나도 없으면 유동인구 규모와 주요 업종 위주로 무난하게 서술",
            "",
            "[수치 표현 규칙]",
            "- 모든 수치는 어림값으로 표현: 45230 → \"약 4만5천명\", 8.34 → \"약 8%\", 22.3 → \"약 22년\"",
            "- 소수점을 그대로 노출하지 않음",
            "",
            "[공통 규칙]",
            "- 수치를 단순 나열하지 말고 그 수치가 어떤 의미인지 풀어서 서술",
            "- 각 문장은 친근한 존댓말 \"~해요/~있어요\"체로 작성",
            "- 값이 null인 필드는 데이터가 없다는 뜻이니 언급하지 말고, 추측해서 채우지도 않음",
            "- 입력에 없는 사실을 지어내지 않음",
            "",
            "예시 입력:",
            "{\"sggNm\":\"강남구\",\"hjdNm\":\"역삼1동\",\"dailyFlpop\":45230,\"flpopChgRatePercent\":12.4,"
                    + "\"topIndutyNm\":\"한식음식점\",\"topIndutyStorCnt\":142,\"majorAgeGrp\":\"30대\","
                    + "\"majorAgeRatioPercent\":28.6,\"avgBldAgeYears\":18.2,\"bldCnt\":387}",
            "",
            "예시 출력:",
            "역삼1동은 유동인구가 전 분기 대비 약 12% 늘어난 일평균 약 4만5천명 수준으로, 인근 행정동 중에서도 "
                    + "활발한 상권에 속해요. 업종은 한식음식점이 142개로 가장 많아 음식점이 밀집된 상권 특성을 보이고, "
                    + "주요 유동인구층은 30대가 약 29%를 차지해 젊은 직장인 상권에 가까운 모습이에요. "
                    + "건물 평균 연한은 약 18년으로 비교적 최근에 지어진 건물이 많은 편이에요."
    );

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
