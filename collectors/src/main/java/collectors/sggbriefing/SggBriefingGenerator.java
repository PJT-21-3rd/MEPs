package collectors.sggbriefing;

import collectors.common.Db;
import collectors.common.OpenAiClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class SggBriefingGenerator {

    private static final String SELECT_SGG_ROW_SQL =
            "SELECT s.sgg_nm, b.ai_brf " +
                    "FROM sgg_ai_briefing b " +
                    "JOIN sgg s ON s.sgg_cd = b.sgg_cd " +
                    "WHERE b.sgg_cd = ?";

    private static final String UPDATE_BRIEFING_SQL =
            "UPDATE sgg_ai_briefing " +
                    "SET ai_brf = ?, ai_model_nm = ?, generated_at = NOW() " +
                    "WHERE sgg_cd = ?";

    private static final String SYSTEM_PROMPT =
            "당신은 서울시 상권/생활인구 데이터를 요약하는 애널리스트입니다. " +
                    "사용자 메시지는 한 구(시군구)의 통계를 담은 JSON입니다. " +
                    "이를 바탕으로 3~4문장의 자연스러운 한국어 브리핑을 작성하세요. " +
                    "수치를 단순 나열하지 말고 의미 있는 특징 위주로 서술하세요. " +
                    "값이 null인 필드는 데이터가 없다는 뜻이니 언급하지 말고, 추측해서 채우지도 마세요.";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private record BriefingContext(
            String sggNm,
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

    private final SggBriefingExtractor extractor = new SggBriefingExtractor();
    private final SggBriefingCalculator calculator = new SggBriefingCalculator();
    private final SggBriefingLoader loader = new SggBriefingLoader();
    private final OpenAiClient openAiClient;

    public SggBriefingGenerator(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    /** hjd_flpop/hjd_store 집계 -> sgg_ai_briefing upsert -> 통계가 바뀌었거나 ai_brf가 없으면 AI 생성 */
    public void generateOne(Connection conn, String sggCd) throws SQLException {
        String targetYyqu = extractor.resolveTargetYyqu(conn);
        String prevYyqu = extractor.resolvePrevYyqu(conn, targetYyqu);
        int targetDays = calculator.daysInQuarter(targetYyqu);
        int prevDays = prevYyqu != null ? calculator.daysInQuarter(prevYyqu) : 0;

        SggBriefingExtractor.SggFlpopTotal flpop = extractor.fetchFlpopTotal(conn, sggCd, targetYyqu);
        if (flpop == null) {
            System.out.printf("[SKIP] %s: %s 분기 유동인구 데이터가 없습니다.%n", sggCd, targetYyqu);
            return;
        }
        Integer prevTotFlpop = extractor.fetchTotalFlpop(conn, sggCd, prevYyqu);
        SggBriefingExtractor.TopStore topStore = extractor.fetchTopStore(conn, sggCd, targetYyqu);

        SggBriefingLoader.Stat stat = calculator.aggregate(flpop, prevTotFlpop, topStore, targetDays, prevDays);
        SggBriefingLoader.Stat existing = extractor.fetchExistingStat(conn, sggCd);
        loader.upsert(conn, sggCd, stat, existing);
        conn.commit();

        try (PreparedStatement stmt = conn.prepareStatement(SELECT_SGG_ROW_SQL)) {
            stmt.setString(1, sggCd);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    System.err.printf("[FAIL] %s: upsert 직후 조회 실패%n", sggCd);
                    return;
                }
                if (rs.getString("ai_brf") != null) {
                    System.out.printf("[SKIP] %s: 통계 변경 없음, 브리핑 재생성 불필요%n", sggCd);
                    return;
                }

                String sggNm = rs.getString("sgg_nm");
                SggBriefingCalculator.BldAgeStat bldAgeStat =
                        calculator.computeBldAgeStat(extractor.fetchHjdStats(conn, sggCd));

                String userPrompt = buildUserPrompt(sggNm, stat, bldAgeStat);
                OpenAiClient.BriefingResult result = openAiClient.generateBriefing(SYSTEM_PROMPT, userPrompt);
                updateBriefing(conn, sggCd, result.text(), result.modelName());
                conn.commit();
                System.out.printf("[OK] %s 브리핑 생성 완료%n", sggCd);
            }
        } catch (Exception e) {
            System.err.printf("[FAIL] %s 브리핑 생성 실패: %s%n", sggCd, e.getMessage());
        }
    }

    public void generateAll(Connection conn) throws SQLException {
        List<String> sggCds = extractor.fetchAllSggCds(conn);
        int total = sggCds.size();
        int index = 0;
        for (String sggCd : sggCds) {
            index++;
            System.out.printf("(%d/%d) %s 처리 중...%n", index, total, sggCd);
            generateOne(conn, sggCd);
        }
    }

    public static void main(String[] args) throws SQLException {
        long startTime = System.currentTimeMillis();
        OpenAiClient openAiClient = new OpenAiClient();
        SggBriefingGenerator generator = new SggBriefingGenerator(openAiClient);

        try (Connection conn = Db.connect()) {
            conn.setAutoCommit(false);
            generator.generateAll(conn);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("=========================================");
        System.out.printf("구(시군구) AI 브리핑 생성 완료 (소요시간: %.2f초)%n", (endTime - startTime) / 1000.0);
        System.out.println("=========================================");
    }

    private static String buildUserPrompt(
            String sggNm, SggBriefingLoader.Stat stat, SggBriefingCalculator.BldAgeStat bldAgeStat
    ) throws JsonProcessingException {
        BriefingContext context = new BriefingContext(
                sggNm,
                stat.dailyFlpop(),
                stat.flpopChgRate(),
                stat.topIndutyNm(),
                stat.topIndutyStorCnt(),
                stat.majorAgeGrp(),
                stat.majorAgeRatio(),
                bldAgeStat.avgBldAge(),
                bldAgeStat.bldCnt() == 0 ? null : bldAgeStat.bldCnt()
        );
        return OBJECT_MAPPER.writeValueAsString(context);
    }

    private static void updateBriefing(Connection conn, String sggCd, String briefingText, String modelName)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(UPDATE_BRIEFING_SQL)) {
            stmt.setString(1, briefingText);
            stmt.setString(2, modelName);
            stmt.setString(3, sggCd);
            stmt.executeUpdate();
        }
    }
}
