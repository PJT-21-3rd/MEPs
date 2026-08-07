package org.meps.report.service;

import lombok.RequiredArgsConstructor;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.common.llm.OpenAiClient;
import org.meps.common.util.SafetyGrade;
import org.meps.report.dto.BriefingInput;
import org.meps.report.dto.SafetyBriefingDto;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 핵심 브리핑(종합 한줄 + 항목별 한줄) 생성.
 *
 * 원칙:
 * - 판정은 룰 엔진이 확정하고 LLM은 문장화만 한다 ("사실 표시, 평가 아님")
 * - 건물당 OpenAI 1회 호출로 5문장을 JSON 일괄 수신 (비용·톤 일관성)
 * - 실패 시 등급별 템플릿 폴백 — 폴백은 DB에 저장하지 않아 다음 요청에서 재시도된다
 */
@Service
@RequiredArgsConstructor
public class BriefingService {

    // 예시는 입력, 출력 쌍으로 제공 (출력만 주면 모델이 입력과 무관하게 예시 문형을 복사)
    // 톤은 친근한 해요체 — 사실 기반은 유지하되 딱딱하지 않게 (팀 결정 2026-08-07)
    private static final String SYSTEM_PROMPT = String.join("\n",
            "당신은 상업용 건물 안전진단 리포트의 요약 문장을 작성합니다.",
            "규칙:",
            "- 반드시 JSON만 출력: {\"overall\":\"...\",\"structure\":\"...\",\"fire\":\"...\",\"sinkhole\":\"...\",\"flood\":\"...\"}",
            "- 모든 값은 친근한 존댓말 \"~해요/~있어요\"체 1~2문장(40~80자)",
            "- 각 항목은 그 항목 라인의 사실만 요약하고, 수치는 어림값으로: 404m → \"약 400m\", 19.3건 → \"약 19건\"",
            "- 입력에 없는 수치·사실을 만들지 않음",
            "- overall: 수치·기관명 없이 주목할 점 1~2개만 요약한 한 문장. 결론·안심 문구는 시스템이 붙이므로 쓰지 않음",
            "- 화재 건수는 건물이 아닌 행정동 통계 — 언급할 때 반드시 \"행정동\"을 붙여 서술 (예: \"행정동의 화재 건수가\")",
            "- 사고 이력·차량 통행 불가 접면 등 불리한 사실은 절대 생략하지 말고 유리한 사실보다 먼저 언급",
            "",
            "예시 입력:",
            "[종합] 등급: 양호",
            "[구조] 등급: 양호 / 주구조: 철근콘크리트구조 / 사용승인: 1978년",
            "[화재] 등급: 주의 / 주구조: 철근콘크리트구조 / 도로접면: 세로한면(불)(차량 통행 불가) / 최근접 소방서: 종로소방서 400m(골든타임 내) / 행정동 최근 3년 평균 화재: 12.3건(서울 행정동 중 상위 25%)",
            "[지반침하] 등급: 안전 / 반경 500m 내 지반침하 사고 이력: 없음",
            "[침수] 등급: 안전 / 저지대 여부: 해당 없음 / 최근 5년 침수 이력: 없음",
            "",
            "예시 출력:",
            "{\"overall\":\"근처 지반침하 이력은 없지만, 소방차 진입이 어려운 좁은 도로 접면을 가지고 있어요.\","
                    + "\"structure\":\"철근콘크리트 구조로 안정적이에요.\","
                    + "\"fire\":\"소방차 진입이 어려운 좁은 도로에 접해 있어요. 가까운 소방서는 약 400m 거리라 골든타임 내 출동은 가능해요.\","
                    + "\"sinkhole\":\"반경 500m 내 지반침하 사고 이력이 없어요.\","
                    + "\"flood\":\"저지대가 아니며 최근 5년간 침수 이력이 없어요.\"}");

    // 프롬프트가 요구하는 40~70자를 다소 벗어나도 서비스 품질엔 문제없어 검증은 느슨한 범위로 건다
    private static final int BRIEF_MIN_LENGTH = 10;
    private static final int BRIEF_MAX_LENGTH = 150;

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    /** building_safety_report.ai_model_nm 기록용 */
    public String getModelName() {
        return openAiClient.getModel();
    }

    /** 5문장 생성. 호출·파싱·검증 어느 단계든 실패하면 LlmCallFailedException */
    public SafetyBriefingDto generate(BriefingInput input) {
        String content = openAiClient.completeJson(SYSTEM_PROMPT, buildUserPrompt(input));

        JsonNode root;
        try {
            root = objectMapper.readTree(content);
        } catch (Exception e) {
            throw new LlmCallFailedException("브리핑 JSON 파싱 실패: " + content, e);
        }

        String overall = root.path("overall").asText(null);
        validateSentence("overall", overall);

        return SafetyBriefingDto.builder()
                .totalBrief(overall + " " + closingPhrase(input))
                .structBrief(factorSentence("structure", "구조", input.getStructFacts(), root))
                .fireBrief(factorSentence("fire", "화재", input.getFireFacts(), root))
                .sinkBrief(factorSentence("sinkhole", "지반침하", input.getSinkFacts(), root))
                .floodBrief(factorSentence("flood", "침수", input.getFloodFacts(), root))
                .build();
    }

    /**
     * 사실이 "정보 없음"뿐인 팩터는 LLM 문장을 버리고 고정 문장으로 대체 — 프롬프트 예시나
     * 다른 항목의 사실이 섞여 들어오는 환각을 코드 레벨에서 차단한다 (실측: 구조 결측 시
     * 예시 출력의 "철근콘크리트"를 끌어와 모순 문장을 생성)
     */
    private String factorSentence(String key, String label, String facts, JsonNode root) {
        if (BriefingInput.NO_FACTS.equals(facts)) {
            return label + " 관련 정보는 아직 확인되지 않았어요.";
        }
        String sentence = root.path(key).asText(null);
        validateSentence(key, sentence);
        return sentence;
    }

    /**
     * 종합 마무리 문구 — LLM에 맡기지 않고 코드에서 확정해 뒤에 붙인다 (등급 판정은 룰 엔진
     * 소관이라는 원칙 + 마무리 표현 누락·변형이 반복된 실측 대응).
     * 정보 없음 팩터가 있으면 안심 표현을 한 단계 보수적으로 내리고 미확인 단서를 덧붙인다
     */
    String closingPhrase(BriefingInput input) {
        List<String> missing = missingFactorLabels(input);
        String phrase;
        if (input.getTotalGrade() == SafetyGrade.CAUTION) {
            phrase = "꼼꼼히 확인해 보시면 좋겠어요.";
        } else if (input.getTotalGrade() == SafetyGrade.GOOD) {
            phrase = "전반적으로 양호한 편이에요.";
        } else if (missing.isEmpty()) {
            phrase = "안심하고 검토하셔도 좋아요.";
        } else {
            phrase = "확인된 항목들은 안정적인 편이에요.";
        }
        if (!missing.isEmpty()) {
            phrase += " 다만 " + String.join("·", missing) + " 정보는 아직 확인되지 않았어요.";
        }
        return phrase;
    }

    private List<String> missingFactorLabels(BriefingInput input) {
        List<String> labels = new ArrayList<>();
        if (BriefingInput.NO_FACTS.equals(input.getStructFacts())) {
            labels.add("구조");
        }
        if (BriefingInput.NO_FACTS.equals(input.getFireFacts())) {
            labels.add("화재");
        }
        if (BriefingInput.NO_FACTS.equals(input.getSinkFacts())) {
            labels.add("지반침하");
        }
        if (BriefingInput.NO_FACTS.equals(input.getFloodFacts())) {
            labels.add("침수");
        }
        return labels;
    }

    /** 등급별 고정 템플릿 폴백. 명세상 브리핑이 Mandatory라 LLM 장애 시에도 응답을 채운다 */
    public SafetyBriefingDto fallback(BriefingInput input) {
        return SafetyBriefingDto.builder()
                .totalBrief(totalFallbackLead(input.getTotalGrade()) + " " + closingPhrase(input))
                .structBrief(factorFallbackSentence("구조", input.getStructGrade()))
                .fireBrief(factorFallbackSentence("화재", input.getFireGrade()))
                .sinkBrief(factorFallbackSentence("지반침하", input.getSinkGrade()))
                .floodBrief(factorFallbackSentence("침수", input.getFloodGrade()))
                .build();
    }

    /** 룰 엔진 결과를 사실 나열로 조립 */
    String buildUserPrompt(BriefingInput input) {
        StringBuilder sb = new StringBuilder();
        sb.append("[종합] 등급: ").append(input.getTotalGrade().getLabel()).append('\n');
        sb.append(factorLine("구조", input.getStructGrade(), input.getStructFacts())).append('\n');
        sb.append(factorLine("화재", input.getFireGrade(), input.getFireFacts())).append('\n');
        sb.append(factorLine("지반침하", input.getSinkGrade(), input.getSinkFacts())).append('\n');
        sb.append(factorLine("침수", input.getFloodGrade(), input.getFloodFacts()));
        return sb.toString();
    }

    /**
     * 정보 없음 팩터는 등급 없이 보낸다 — "등급: 안전 / 정보 없음"으로 주면 모델이
     * overall에서 "구조가 안전"이라고 단정하는 재료가 된다 (실측)
     */
    private String factorLine(String label, SafetyGrade grade, String facts) {
        if (BriefingInput.NO_FACTS.equals(facts)) {
            return "[" + label + "] " + BriefingInput.NO_FACTS;
        }
        return "[" + label + "] 등급: " + grade.getLabel() + " / " + facts;
    }

    private void validateSentence(String key, String sentence) {
        if (sentence == null || sentence.isBlank()) {
            throw new LlmCallFailedException("브리핑 누락: " + key);
        }
        // 화재 건수는 행정동 통계 — 출처 없이 쓰면 건물 자체 이력처럼 읽혀 검증에서 걸러낸다
        if (sentence.contains("화재 건") && !sentence.contains("행정동")) {
            throw new LlmCallFailedException("화재 건수에 행정동 표기 누락(" + key + "): " + sentence);
        }
        if (sentence.length() < BRIEF_MIN_LENGTH || sentence.length() > BRIEF_MAX_LENGTH) {
            throw new LlmCallFailedException("브리핑 길이 이탈(" + key + ", " + sentence.length() + "자): " + sentence);
        }
    }

    private String totalFallbackLead(SafetyGrade grade) {
        if (grade == SafetyGrade.CAUTION) {
            return "일부 항목에서 확인이 필요한 이력이 있어요.";
        }
        if (grade == SafetyGrade.GOOD) {
            return "큰 특이 사항 없이 확인되는 건물이에요.";
        }
        return "주요 진단 항목에서 특이 이력이 확인되지 않았어요.";
    }

    private String factorFallbackSentence(String label, SafetyGrade grade) {
        if (grade == SafetyGrade.CAUTION) {
            return label + " 항목에서 확인이 필요한 이력이 있어요.";
        }
        if (grade == SafetyGrade.GOOD) {
            return label + " 항목은 양호한 수준으로 확인돼요.";
        }
        return label + " 항목에서 특이 이력은 확인되지 않았어요.";
    }
}
