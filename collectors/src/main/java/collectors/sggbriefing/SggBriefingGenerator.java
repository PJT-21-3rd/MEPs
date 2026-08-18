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

    private static final String SYSTEM_PROMPT = String.join("\n",
            "당신은 서울시 상권/생활인구 데이터를 요약하는 애널리스트입니다.",
            "사용자 메시지는 한 구(시군구, sggNm)의 통계를 담은 JSON입니다.",
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
            "{\"sggNm\":\"강남구\",\"dailyFlpop\":312400,\"flpopChgRatePercent\":9.1,"
                    + "\"topIndutyNm\":\"한식음식점\",\"topIndutyStorCnt\":1284,\"majorAgeGrp\":\"30대\","
                    + "\"majorAgeRatioPercent\":26.8,\"avgBldAgeYears\":21.5,\"bldCnt\":15230}",
            "",
            "예시 출력:",
            "강남구는 일평균 유동인구가 약 31만명 수준으로 서울 내에서도 유동인구가 매우 활발한 구에 속해요. "
                    + "전 분기 대비로는 약 9% 늘어나며 완만한 증가세를 보이고 있어요. 업종은 한식음식점이 1284개로 "
                    + "가장 많아 음식점 상권이 두드러지고, 주요 유동인구층은 30대가 약 27%를 차지해 젊은 직장인층이 "
                    + "두터운 편이에요. 건물 평균 연한은 약 22년으로 비교적 오래된 건물이 섞여 있는 지역이에요."
    );

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
