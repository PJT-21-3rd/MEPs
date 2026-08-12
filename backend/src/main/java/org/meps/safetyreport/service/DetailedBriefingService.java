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
            "당신은 상업용 건물 안전진단 상세 리포트의 해석을 작성합니다.",
            "규칙:",
            "- 반드시 JSON만 출력: {\"overall\":{\"headline\":\"...\",\"summary\":\"...\"},"
                    + "\"structure\":\"...\",\"fire\":\"...\",\"sinkhole\":\"...\",\"flood\":\"...\"}",
            "",
            "[종합(overall) 작성 규칙]",
            "- headline: 전체 진단을 압축한 한 문장(30~60자). 큰따옴표나 특수 인용부호로 감싸지 않음",
            "- summary: 4개 항목을 아우르는 종합 해석 2~3문장(80~200자). 근거/리스크/솔루션을 라벨로",
            "  나누지 말고 자연스러운 흐름으로 서술. 불리한 사실(사고 이력, 접근성 문제 등)을 먼저",
            "  언급하고, 마지막 문장에서 아래 항목별 상세 진단 확인을 권유",
            "- 개별 항목의 세부 수치를 다시 나열하지 말고 특히 주목할 항목만 짚어서 언급",
            "",
            "[항목별(구조/화재/지반침하/침수) 작성 규칙]",
            "- 모든 값은 정확히 3문단으로 구성: 1문단 근거, 2문단 리스크, 3문단 솔루션",
            "- \"근거:\", \"리스크:\", \"솔루션:\" 같은 라벨 없이 문단 내용만 서술",
            "- 문단 사이는 반드시 줄바꿈 두 번(\\n\\n)으로 구분. 위 예시 출력의 \\n\\n 위치를 그대로 따를 것",
            "- 1문단(근거): 입력에 주어진 사실만으로 등급이 그렇게 나온 이유를 정리 (등급 자체를 재해석하거나 다른 등급을 제시하지 않음). 수치를 나열만 하지 말고 그 수치가 왜 이 등급으로 이어지는지 풀어서 설명",
            "- 2문단(리스크): 그 사실이 실거주·임차·매입 관점에서 어떤 의미인지, 우려되는 지점이 있다면 무엇인지 서술. 위험 요인이 없는 항목도 문단을 짧게 끝내지 말고, 그 위험 요인이 일반적으로 왜 문제가 되는지와 이 건물엔 왜 해당하지 않는지를 함께 설명",
            "- 3문단(솔루션): 사용자가 취할 수 있는 구체적인 확인·대응 방법을 1~2가지 제안. 위험이 없는 경우에도 \\\"참고만 하세요\\\"로 짧게 끝내지 말고, 어떤 상황이 되면 다시 확인해봐야 하는지 등 실질적인 조언을 덧붙임. 과장된 안심이나 근거 없는 낙관은 금지",
            "- 위 배경 설명은 그 위험 요인 자체에 대한 일반적인 지식(예: \"지반침하는 보통 지하 굴착 공사로 발생한다\")까지는 써도 되지만, 이 건물에 대해 입력에 없는 사실을 지어내는 것은 여전히 금지",
            "- 각 문단은 친근한 존댓말 \"~해요/~있어요\"체 각 문단은 2~3문장으로 충분히 설명. 한 문장으로 끝내지 말 것 — 위 예시 출력 정도의 분량(문단당 대략 60~120자)을 기준으로 삼을 것",
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
                    + "\"summary\":\"구조와 지반은 안정적이라 붕괴나 침하 위험은 적어요. 다만 화재 항목은 좁은 도로 접면 때문에"
                    + " 소방차 진입이 어려워 주의가 필요하고, 초기 대응 준비를 미리 해두시는 게 좋아요. 아래 항목별 상세"
                    + " 진단을 확인해 보세요.\"},"
                    + "\"structure\":\"1978년에 사용승인을 받은 철근콘크리트구조 건물로, 구조 등급은 양호로 확인돼요."
                    + " 철근콘크리트는 압축과 인장에 고르게 강해 상업용 건물에서 가장 널리 쓰이는 안정적인 구조 방식이에요."
                    + "\\n\\n다만 준공된 지 40년 넘게 지나면서 노후화 가능성을 함께 살펴볼 필요가 있어요."
                    + " 철근콘크리트 건물은 시간이 지나면 배관 노후화나 외벽의 미세한 균열이 나타날 수 있는데,"
                    + " 이런 변화가 곧바로 큰 구조적 위험으로 이어지진 않지만 방치하면 누수나 마감재 손상으로 번질 수 있어요."
                    + "\\n\\n계약 전에 천장 모서리나 화장실 배관 주변에 누수 흔적이 있는지 꼼꼼히 확인해 보시는 게 좋아요."
                    + " 최근에 안전점검이나 보수 공사를 진행한 이력이 있는지 관리사무소나 건축물대장을 통해 한 번 더 확인해 보시길 권해드려요.\","
                    + "\"fire\":\"이 건물은 세로한면(불) 도로에 접해 있어서 대형 소방차가 통행하기 어려운 좁은 골목 조건이에요."
                    + " 가장 가까운 종로소방서까지는 약 400m로 거리 자체는 골든타임 내에 있지만, 접근로가 좁다는 점이 화재 등급을 주의로"
                    + " 끌어올리는 요인이 됐어요. 행정동 기준 최근 3년 평균 화재 건수도 약 12건으로 서울 행정동 중 상위 25%에 속해요."
                    + "\\n\\n화재가 발생했을 때 소방차가 골목 안까지 신속하게 들어오지 못하면 초기 진화가 늦어질 수 있어요."
                    + " 특히 불법 주정차 차량이 있는 경우 진입이 더 지연될 수 있어서, 거리상으로는 골든타임 내라도"
                    + " 실제 대응 시간은 더 늘어날 가능성이 있어요."
                    + "\\n\\n건물 내부에 소화기와 화재경보기가 규정에 맞게 설치돼 있는지, 비상 대피로가 막혀있지 않은지"
                    + " 미리 확인해 두시는 게 좋아요. 또 골목 초입에 불법 주정차가 잦은 편인지 주변 상황도 한 번 살펴보시길 추천해요.\","
                    + "\"sinkhole\":\"이 건물 반경 500m 이내에서 최근 지반침하(싱크홀) 사고 이력이 단 한 건도 확인되지 않았어요."
                    + " 지반침하는 주로 지하 굴착 공사나 노후 상하수관 손상으로 발생하는데, 이 일대에서는 그런 사고 기록이 없다는 뜻이에요."
                    + "\\n\\n지반이 안정적이라는 건 도로 꺼짐이나 건물 기울어짐 같은 급작스러운 문제로 영업이나 거주에 지장이 생길"
                    + " 가능성이 낮다는 의미예요. 다만 지반침하는 예고 없이 발생할 수 있는 만큼, 이력이 없다는 게 앞으로도"
                    + " 100% 안전하다는 보장은 아니라는 점은 참고해 두시면 좋아요."
                    + "\\n\\n현재로선 별도의 지반 관련 대비를 추가로 하실 필요는 없어요. 다만 주변에서 대규모 지하 굴착 공사가"
                    + " 시작되는 시기가 있다면 그때는 한 번씩 관심을 가지고 지켜보시는 정도면 충분해요.\","
                    + "\"flood\":\"이 건물은 저지대에 해당하지 않고, 최근 5년간 침수 피해 이력도 확인되지 않았어요."
                    + " 침수는 주로 저지대나 배수가 원활하지 않은 지역에서 집중적으로 발생하는데, 이 건물은 그런 조건에 해당하지 않아요."
                    + "\\n\\n침수 이력이 없는 지역이라 하더라도 기록적인 폭우가 내리면 예상치 못한 침수 피해가 생길 가능성은"
                    + " 항상 남아있어요. 다만 지금까지의 기록만 놓고 보면 이 건물이 특별히 취약한 조건은 아니라고 볼 수 있어요."
                    + "\\n\\n당장 별도의 침수 대비 조치를 하실 필요는 없어요. 다만 장마철이나 태풍 시기에는 배수구 주변에"
                    + " 낙엽이나 쓰레기가 쌓여있지 않은지 정도만 가볍게 확인해 두시면 좋아요.\"}");

    // 프롬프트가 요구하는 글자 수를 다소 벗어나도 서비스 품질엔 문제없어 검증은 느슨한 범위로 건다
    // (BasicBriefingService와 동일한 원칙). headline/summary는 프롬프트 기준(30~60자/80~150자)보다
    // 넉넉히, 항목별 리포트는 3문단(각 2~3문장) 분량을 감안해 넉넉히 잡는다
    private static final int HEADLINE_MIN_LENGTH = 10;
    private static final int HEADLINE_MAX_LENGTH = 100;
    private static final int SUMMARY_MIN_LENGTH = 40;
    private static final int SUMMARY_MAX_LENGTH = 300;
    private static final int REPORT_MIN_LENGTH = 120;
    private static final int REPORT_MAX_LENGTH = 1000;

    private static final int MAX_TOKENS = 4000;

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;
    private final BasicBriefingService basicBriefingService;

//    /** building_safety_report.ai_model_nm 기록용 */
//    public String getModelName() {
//        return openAiClient.getModel();
//    }

    /** 종합(헤드라인+요약) + 항목별 4개 해석 생성. 호출·파싱·검증 어느 단계든 실패하면 LlmCallFailedException */
    public DetailedBriefingDto generate(BriefingInput input) {
        String content = openAiClient.completeJson(SYSTEM_PROMPT, BriefingFactFormatter.buildUserPrompt(input), MAX_TOKENS);

        JsonNode root;
        try {
            root = objectMapper.readTree(content);
        } catch (Exception e) {
            throw new LlmCallFailedException("상세 리포트 JSON 파싱 실패: " + content, e);
        }

        JsonNode overallNode = root.path("overall");
        String headline = overallNode.path("headline").asText(null);
        String summary = overallNode.path("summary").asText(null);
        validateOverallPart("headline", headline, HEADLINE_MIN_LENGTH, HEADLINE_MAX_LENGTH);
        validateOverallPart("summary", summary, SUMMARY_MIN_LENGTH, SUMMARY_MAX_LENGTH);

        return DetailedBriefingDto.builder()
                .totalHeadline(headline)
                .totalSummary(summary)
                .structReport(factorReport("structure", "구조", input.getStructFacts(), root))
                .fireReport(factorReport("fire", "화재", input.getFireFacts(), root))
                .sinkReport(factorReport("sinkhole", "지반침하", input.getSinkFacts(), root))
                .floodReport(factorReport("flood", "침수", input.getFloodFacts(), root))
                .build();
    }

//    /** headline(한 줄) + summary(2~3문장)를 개행으로 이어붙여 최종 종합 리포트 텍스트로 조립 */
//    private String buildOverallReport(JsonNode overallNode) {
//        String headline = overallNode.path("headline").asText(null);
//        String summary = overallNode.path("summary").asText(null);
//        return headline + "\n" + summary;
//    }


    /**
     * 사실이 "정보 없음"뿐인 팩터는 LLM 문장을 버리고 고정 문장으로 대체
     */
    private String factorReport(String key, String label, String facts, JsonNode root) {
        if (BriefingInput.NO_FACTS.equals(facts)) {
            return "근거: " + label + " 관련 정보가 아직 확인되지 않았어요."
                    + "\n\n리스크: 정보가 확인되기 전까지는 관련 위험 여부를 판단하기 어려워요."
                    + "\n\n솔루션: 건축물대장이나 관리사무소를 통해 관련 정보를 추가로 확인해 보세요.";
        }
        String report = root.path(key).asText(null);
        validateReport(key, report);
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

    private void validateOverallPart(String key, String text, int minLength, int maxLength) {
        if (text == null || text.isBlank()) {
            throw new LlmCallFailedException("종합 리포트 누락: " + key);
        }
        checkFireCountDongLabel(key, text);
        if (text.length() < minLength || text.length() > maxLength) {
            throw new LlmCallFailedException("종합 리포트 길이 이탈(" + key + ", " + text.length() + "자): " + text);
        }
    }

    private void validateReport(String key, String report) {
        if (report == null || report.isBlank()) {
            throw new LlmCallFailedException("상세 리포트 해석 누락: " + key);
        }
        // 원칙은 3문단(근거/리스크/솔루션)이지만 "이력 없음" 등 리스크가 사실상 없는 팩터는
        // 근거·리스크가 한 문단으로 합쳐져 나오는 경우가 실측되어 완전히 뭉뚱그려진 1문단만 걸러내고 2문단 이상은 허용한다
        String[] paragraphs = report.split("\n\n");
        if (paragraphs.length < 2) {
            throw new LlmCallFailedException(
                    "상세 리포트 문단 구성 누락(" + key + ", " + paragraphs.length + "문단, 최소 2문단 필요): " + report);
        }
        checkFireCountDongLabel(key, report);
        if (report.length() < REPORT_MIN_LENGTH || report.length() > REPORT_MAX_LENGTH) {
            throw new LlmCallFailedException("상세 리포트 길이 이탈(" + key + ", " + report.length() + "자): " + report);
        }
    }

    /** 화재 건수는 행정동 통계. 출처 없이 쓰면 건물 자체 이력처럼 읽혀 검증에서 걸러낸다 */
    private void checkFireCountDongLabel(String key, String text) {
        if (text.contains("화재 건") && !text.contains("행정동")) {
            throw new LlmCallFailedException("화재 건수에 행정동 표기 누락(" + key + "): " + text);
        }
    }
}
