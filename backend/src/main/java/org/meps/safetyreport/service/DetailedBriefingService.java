package org.meps.safetyreport.service;

import lombok.RequiredArgsConstructor;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.common.llm.OpenAiClient;
import org.meps.common.util.SafetyGrade;
import org.meps.safetyreport.dto.BriefingInput;
import org.meps.safetyreport.dto.DetailedBriefingDto;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * AI 상세 리포트 해석(종합 헤드라인+요약 + 항목별 "근거→리스크→솔루션" 해석) 생성.
 *
 * - 판정은 룰 엔진이 확정하고 LLM은 문장화만 한다 ("사실 표시, 평가 아님") — BasicBriefingService와 동일
 * - 건물당 OpenAI 1회 호출로 종합 2개 + 항목별 4개 해석을 JSON 일괄 수신
 * - 실패 시 등급별 템플릿 폴백 — 폴백은 DB에 저장하지 않아 다음 요청에서 재시도된다
 * - 반환 타입은 전용 DetailedBriefingDto — 종합 파트가 headline/summary 2필드로 나뉘어
 *   BasicBriefingDto(단일 totalBrief)와 shape이 달라 재사용할 수 없다
 */
@Service
@RequiredArgsConstructor
public class DetailedBriefingService {

    private static final String SYSTEM_PROMPT = String.join("\n",
            "당신은 상업용 건물 안전진단 상세 리포트의 항목별 해석을 작성합니다.",
            "규칙:",
            "- 반드시 JSON만 출력: {\"overall\":\"...\",\"structure\":\"...\",\"fire\":\"...\",\"sinkhole\":\"...\",\"flood\":\"...\"}",
            "[종합(overall) 작성 규칙]",
            "- headline: 전체 진단을 압축한 한 문장(30~60자)",
            "- summary: 4개 항목을 아우르는 종합 해석 2~3문장(80~150자)을 자연스러운 흐름으로 서술. 불리한 사실(사고 이력, 접근성 문제 등)을 먼저 언급하고, 마지막 문장에서 아래 항목별 상세 진단 확인을 권유",
            "- 개별 항목의 세부 수치를 다시 나열하지 말고 특히 주목할 항목만 짚어서 언급",
            "",
            "[항목별(구조/화재/지반침하/침수) 작성 규칙]",
            "- 모든 값은 정확히 3문단이며 각 문단은 근거, 리스크, 솔루션을 설명",
            "- 문단 사이는 줄바꿈 두 번(\\n\\n)으로 구분",
            "- 근거 문단: 입력에 주어진 사실만으로 등급이 그렇게 나온 이유를 정리 (등급 자체를 재해석하거나 다른 등급을 제시하지 않음)",
            "- 리스크 문단: 그 사실이 임차·매입 관점에서 어떤 의미인지, 우려되는 지점이 있다면 무엇인지 서술",
            "- 솔루션 문단: 사용자가 취할 수 있는 구체적인 확인·대응 방법을 1~2가지 제안. 과장된 안심이나 근거 없는 낙관은 금지",
            "- 각 문단은 친근한 존댓말 \"~해요/~있어요\"체 2~3문장",
            "",
            "[공통 규칙]",
            "- 각 항목은 그 항목 라인의 사실만 사용하고, 수치는 어림값으로: 404m → \"약 400m\", 19.3건 → \"약 19건\"",
            "- 입력에 없는 수치·사실을 만들지 않음",
            "- 화재 건수는 건물이 아닌 행정동 통계 — 언급할 때 반드시 \"행정동\"을 붙여 서술 (예: \"행정동의 화재 건수가\")",
            "- 사고 이력·차량 통행 불가 접면 등 불리한 사실은 절대 생략하지 말고 유리한 사실보다 먼저 언급",
            "- 보험·대출 등 구체적인 금융상품명은 절대 언급하지 않음 (상품 추천은 별도 화면에서 처리)",
            "",
            "예시 입력:",
            "[종합] 등급: 양호",
            "[구조] 등급: 양호 / 주구조: 철근콘크리트구조 / 사용승인: 1978년",
            "[화재] 등급: 주의 / 주구조: 철근콘크리트구조 / 도로접면: 세로한면(불)(차량 통행 불가) / 최근접 소방서: 종로소방서 400m(골든타임 내) / 행정동 최근 3년 평균 화재: 12.3건(서울 행정동 중 상위 25%)",
            "[지반침하] 등급: 안전 / 반경 500m 내 지반침하 사고 이력: 없음",
            "[침수] 등급: 안전 / 저지대 여부: 해당 없음 / 최근 5년 침수 이력: 없음",
            "",
            "예시 출력:",
            "{\"overall\":{\"headline\":\"장마철 수해와 화재 골든타임 대비가 필요한 건물이에요.\","
                    + "\"summary\":\"구조와 지반은 안정적이라 붕괴나 침하 위험은 적어요. 다만 화재 시에는 좁은 도로 접면 때문에"
                    + " 소방차 진입이 어려워 주의가 필요하고, 초기 대응 준비를 미리 해두시는 게 좋아요. 아래 항목별 상세"
                    + " 진단을 확인해 보세요.\"},"
                    + "\"structure\":\"1978년 사용승인된 철근콘크리트구조로 구조 등급이 양호해요."
                    + " 준공된 지 오래돼 노후화 여부는 별도로 살펴볼 필요가 있어요."
                    + " 최근 안전점검·보수 이력이 있는지 관리사무소나 건축물대장을 통해 확인해 보세요.\","
                    + "\"fire\":\"세로한면(불) 접면이라 소방차 진입이 어렵고, 가장 가까운 종로소방서는 약 400m로 골든타임 내 거리예요."
                    + " 화재 발생 시 초기 진입이 지연될 수 있어 다른 항목보다 주의가 필요해요."
                    + " 건물 내 소화설비와 비상 대피로가 잘 갖춰져 있는지 확인해 보세요.\","
                    + "\"sinkhole\":\"반경 500m 내 지반침하 사고 이력이 없어요."
                    + " 현재까지 확인된 위험 요인은 없어요."
                    + " 별도 조치 없이 참고만 하셔도 좋아요.\","
                    + "\"flood\":\"저지대가 아니고 최근 5년간 침수 이력도 없어요."
                    + " 현재까지 확인된 침수 위험은 없어요."
                    + " 별도 조치 없이 참고만 하셔도 좋아요.\"}");

    // 프롬프트가 요구하는 글자 수를 다소 벗어나도 서비스 품질엔 문제없어 검증은 느슨한 범위로 건다
    // (BasicBriefingService와 동일한 원칙). headline/summary는 프롬프트 기준(30~60자/80~150자)보다
    // 넉넉히, 항목별 리포트는 3문단(각 2~3문장) 분량을 감안해 넉넉히 잡는다
    private static final int HEADLINE_MIN_LENGTH = 15;
    private static final int HEADLINE_MAX_LENGTH = 100;
    private static final int SUMMARY_MIN_LENGTH = 40;
    private static final int SUMMARY_MAX_LENGTH = 250;
    private static final int REPORT_MIN_LENGTH = 60;
    private static final int REPORT_MAX_LENGTH = 500;

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private final BasicBriefingService basicBriefingService;

    /** building_safety_report.ai_model_nm 기록용 */
    public String getModelName() {
        return openAiClient.getModel();
    }

    /** 종합(헤드라인+요약) + 항목별 4개 해석 생성. 호출·파싱·검증 어느 단계든 실패하면 LlmCallFailedException */
    public DetailedBriefingDto generate(BriefingInput input) {
        String content = openAiClient.completeJson(SYSTEM_PROMPT, basicBriefingService.buildUserPrompt(input));

        JsonNode root;
        try {
            root = objectMapper.readTree(content);
        } catch (Exception e) {
            throw new LlmCallFailedException("상세 리포트 JSON 파싱 실패: " + content, e);
        }

        // overall은 {"headline":"...","summary":"..."} 객체 — 단순 문자열이 아니므로 하위 필드를 각각 꺼낸다
        JsonNode overallNode = root.path("overall");
        String headline = overallNode.path("headline").asText(null);
        String summary = overallNode.path("summary").asText(null);
        validateLength("overall.headline", headline, HEADLINE_MIN_LENGTH, HEADLINE_MAX_LENGTH);
        validateLength("overall.summary", summary, SUMMARY_MIN_LENGTH, SUMMARY_MAX_LENGTH);

        return DetailedBriefingDto.builder()
                .totalHeadline(headline)
                .totalSummary(summary)
                .structReport(factorReport("structure", "구조", input.getStructFacts(), root))
                .fireReport(factorReport("fire", "화재", input.getFireFacts(), root))
                .sinkReport(factorReport("sinkhole", "지반침하", input.getSinkFacts(), root))
                .floodReport(factorReport("flood", "침수", input.getFloodFacts(), root))
                .build();
    }

    /**
     * 사실이 "정보 없음"뿐인 팩터는 LLM 문장을 버리고 고정 문장으로 대체 — BasicBriefingService와
     * 동일한 이유(다른 항목·예시의 사실이 섞여 들어오는 환각을 코드 레벨에서 차단)
     */
    private String factorReport(String key, String label, String facts, JsonNode root) {
        if (BriefingInput.NO_FACTS.equals(facts)) {
            return label + " 관련 정보가 아직 확인되지 않아 상세 해석을 제공할 수 없어요.";
        }
        String report = root.path(key).asText(null);
        validateLength(key, report, REPORT_MIN_LENGTH, REPORT_MAX_LENGTH);
        // 화재 건수는 행정동 통계 — 출처 없이 쓰면 건물 자체 이력처럼 읽혀 검증에서 걸러낸다
        if (report.contains("화재 건") && !report.contains("행정동")) {
            throw new LlmCallFailedException("화재 건수에 행정동 표기 누락(" + key + "): " + report);
        }
        return report;
    }

    /** 등급별 고정 템플릿 폴백. 명세상 aiReport가 Mandatory라 LLM 장애 시에도 응답을 채운다 */
    public DetailedBriefingDto fallback(BriefingInput input) {
        return DetailedBriefingDto.builder()
                .totalHeadline(fallbackHeadline(input.getTotalGrade()))
                .totalSummary(fallbackSummary(input))
                .structReport(fallbackFactorReport("구조", input.getStructGrade()))
                .fireReport(fallbackFactorReport("화재", input.getFireGrade()))
                .sinkReport(fallbackFactorReport("지반침하", input.getSinkGrade()))
                .floodReport(fallbackFactorReport("침수", input.getFloodGrade()))
                .build();
    }

    private String fallbackHeadline(SafetyGrade grade) {
        if (grade == SafetyGrade.CAUTION) {
            return "일부 항목에서 확인이 필요한 이력이 있는 건물이에요.";
        }
        if (grade == SafetyGrade.GOOD) {
            return "전반적으로 양호한 수준의 건물이에요.";
        }
        return "주요 진단 항목에서 특이 이력이 확인되지 않은 건물이에요.";
    }

    private String fallbackSummary(BriefingInput input) {
        return "종합 등급은 " + input.getTotalGrade().getLabel() + "이에요. 4개 항목의 세부 근거와 해석은 "
                + "아래 항목별 상세 진단에서 확인해 보세요.";
    }

    private String fallbackFactorReport(String label, SafetyGrade grade) {
        if (grade == SafetyGrade.CAUTION) {
            return label + " 항목은 세부 근거 중 확인이 필요한 이력이 있어 주의 등급으로 판정됐어요. "
                    + "어떤 사실이 영향을 줬는지는 위 상세 근거 항목에서 확인하실 수 있어요. "
                    + "해당 이력의 세부 내용을 꼼꼼히 살펴보고, 필요하면 관리사무소나 관련 기관에 추가로 확인해 보세요.";
        }
        if (grade == SafetyGrade.GOOD) {
            return label + " 항목은 세부 근거가 대체로 양호한 수준으로 확인돼 양호 등급으로 판정됐어요. "
                    + "큰 위험 요인은 확인되지 않았지만 완전히 안전을 보장하는 것은 아니에요. "
                    + "위 상세 근거 항목을 참고해 관심 있는 부분이 있다면 추가로 확인해 보세요.";
        }
        return label + " 항목은 세부 근거에서 특이 이력이 확인되지 않아 안전 등급으로 판정됐어요. "
                + "현재까지 확인된 위험 요인은 없어요. "
                + "별도 조치 없이 위 상세 근거 항목을 참고만 하셔도 좋아요.";
    }

    private void validateLength(String key, String text, int minLength, int maxLength) {
        if (text == null || text.isBlank()) {
            throw new LlmCallFailedException("상세 리포트 해석 누락: " + key);
        }
        if (text.length() < minLength || text.length() > maxLength) {
            throw new LlmCallFailedException("상세 리포트 길이 이탈(" + key + ", " + text.length() + "자): " + text);
        }
    }
}
