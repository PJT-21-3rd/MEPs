package org.meps.safetyreport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.common.llm.OpenAiClient;
import org.meps.common.util.SafetyGrade;
import org.meps.safetyreport.dto.BriefingInput;
import org.meps.safetyreport.dto.BasicBriefingDto;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class BasicBriefingService {

    // 예시는 입력, 출력 쌍으로 제공 (출력만 주면 모델이 입력과 무관하게 예시 문형을 복사)
    // 톤은 친근한 해요체 — 사실 기반은 유지하되 딱딱하지 않게 (팀 결정 2026-08-07)
    private static final String SYSTEM_PROMPT = String.join("\n",
            "당신은 상업용 건물 안전진단 리포트의 요약 문장을 작성합니다.",
            "규칙:",
            "- 반드시 JSON만 출력: {\"overall\":\"...\",\"structure\":\"...\",\"fire\":\"...\",\"sinkhole\":\"...\",\"flood\":\"...\"}",
            "- 모든 값은 친근한 존댓말 \"~해요/~있어요\"체 1~2문장(40~80자)",
            "- 각 항목은 그 항목 라인의 사실만 요약하고, 수치는 어림값으로: 404m → \"약 400m\", 19.3건 → \"약 19건\"",
            "- 입력에 없는 수치·사실을 만들지 않음",
            "- overall: 수치·기관명 없이 주목할 점 1~2개만 요약한 한 문장. 결론·안심 문구는 시스템이 붙이므로 쓰지 않음."
                    + " 같은 사실을 다른 표현으로 반복하지 않음(예: \"화재 건수가 높은 편이고 많아요\"처럼 같은 내용을 두 번 말하지 않음)."
                    + " 주목할 점은 각 항목 사실이 듣기에 부정적인 정도가 아니라, 등급이 가장 낮은(주의에 가장 가까운) 항목을"
                    + " 우선 선택함 (예: [구조] 양호, [화재] 안전이면 화재가 아니라 구조를 우선 언급)."
                    + " 화재 건수·분위(예: \"12.3건\", \"상위 25%\") 와 관련된 내용은 overall에 넣지 않음"
                    + " 건수 대신 도로접면 등 다른 사실로 언급하거나, 화재 외 항목이 있다면 그 항목을 우선함. 화재 건수 상세는 fire 필드에서만 다룸.",
            "- 화재 건수는 건물이 아닌 행정동 통계 — 언급할 때 반드시 \"행정동\"을 붙여 서술 (예: \"행정동의 화재 건수가\")",
            "- 사고 이력·차량 통행 불가 접면 등 불리한 사실은 절대 생략하지 말고 유리한 사실보다 먼저 언급",
            "- 어조는 반드시 그 문장이 속한 항목([종합]/[구조]/[화재]/[지반침하]/[침수]) 라인에 표시된 등급을 따른다."
                    + " 등급이 안전·양호이면 그 항목에 불리한 사실이 있어도 \"위험\", \"우려\", \"불안\", \"취약\", \"위협\","
                    + " \"주의가 필요\", \"조심\", \"걱정\" 같은 경고 표현을 쓰지 않고 사실만 담담히 서술한다"
                    + " (예: \"화재 건수가 많은 편이에요\"는 되지만 \"화재 건수가 많아 우려돼요\"는 안 됨)."
                    + " 등급이 주의이면 \"안심\", \"양호한 편\", \"안전한 편\", \"문제없\", \"안정적\", \"견고\", \"튼튼\" 같은"
                    + " 안심 표현을 쓰지 않는다 (예: \"벽돌구조로 비교적 안정적이에요\"는 안 되고 \"벽돌구조로 노후화 확인이 필요해요\"처럼 씀)",
            "",
            "예시 입력:",
            "[종합] 등급: 양호",
            "[구조] 등급: 양호 / 주구조: 철근콘크리트구조 / 사용승인: 1978년",
            "[화재] 등급: 주의 / 주구조: 철근콘크리트구조 / 도로접면: 세로한면(불)(차량 통행 불가) / 최근접 소방서: 종로소방서 400m(골든타임 내) / 행정동 최근 3년 평균 화재: 12.3건(서울 행정동 중 상위 25%)",
            "[지반침하] 등급: 안전 / 반경 500m 내 지반침하 사고 이력: 없음",
            "[침수] 등급: 안전 / 저지대 여부: 해당 없음 / 최근 5년 침수 이력: 없음",
            "",
            "예시 출력:",
            "{\"overall\":\"화재 항목에서 좁은 도로 접면 때문에 확인이 필요한 건물이에요.\","
                    + "\"structure\":\"철근콘크리트 구조로 안정적이에요.\","
                    + "\"fire\":\"소방차 진입이 어려운 좁은 도로에 접해 있어요. 가까운 소방서는 약 400m 거리라 골든타임 내 출동은 가능해요.\","
                    + "\"sinkhole\":\"반경 500m 내 지반침하 사고 이력이 없어요.\","
                    + "\"flood\":\"저지대가 아니며 최근 5년간 침수 이력이 없어요.\"}",
            "",

            "예시 입력 2:",
            "[종합] 등급: 주의",
            "[구조] 등급: 주의 / 주구조: 벽돌구조 / 사용승인: 1988년",
            "[화재] 등급: 안전 / 주구조: 벽돌구조 / 도로접면: 광대한면(폭 25m 이상 도로 접함) / 최근접 소방서: 종로소방서 300m(골든타임 내) / 행정동 최근 3년 평균 화재: 5.0건(서울 행정동 중 하위 25%, 적은 편)",
            "[지반침하] 등급: 주의 / 반경 500m 내 지반침하 사고 이력: 2건 / 최근 사고: 2023년 5월, 거리 120m",
            "[침수] 등급: 안전 / 저지대 여부: 해당 없음 / 최근 5년 침수 이력: 없음",
            "",
            "예시 출력 2:",
            "{\"overall\":\"구조와 지반침하 두 항목에서 확인이 필요한 건물이에요.\","
                    + "\"structure\":\"벽돌구조이고 사용승인은 1988년으로, 노후화 여부를 확인해 볼 필요가 있어요.\","
                    + "\"fire\":\"광대한 도로에 접해 있고 행정동 화재 건수도 적은 편이에요. 가까운 소방서는 약 300m 거리라 골든타임 내 출동이 가능해요.\","
                    + "\"sinkhole\":\"반경 500m 내 지반침하 사고 이력이 2건 있고, 가장 최근 사고는 2023년 5월 약 120m 거리에서 있었어요. 확인이 필요해요.\","
                    + "\"flood\":\"저지대가 아니며 최근 5년간 침수 이력이 없어요.\"}",
            "",

            "예시 입력 3:",
            "[종합] 등급: 안전",
            "[구조] 등급: 양호 / 주구조: 철근콘크리트구조 / 사용승인: 1969년",
            "[화재] 등급: 안전 / 주구조: 철근콘크리트구조 / 도로접면: 세로한면(가)(폭 8m 미만 도로 접함) / 최근접 소방서: 회현119안전센터 540m(골든타임 내) / 행정동 최근 3년 평균 화재: 18.3건(서울 행정동 중 상위 25%, 많은 편)",
            "[지반침하] 등급: 안전 / 반경 500m 내 지반침하 사고 이력: 없음",
            "[침수] 등급: 안전 / 저지대 여부: 해당 없음 / 최근 5년 침수 이력: 없음",
            "",
            "예시 출력 3:",
            "{\"overall\":\"구조 항목에서 확인이 필요한 부분이 있는 건물이에요.\","
                    + "\"structure\":\"철근콘크리트구조로 안정적이지만 사용승인일이 1969년으로, 노후화 여부를 확인해 볼 필요가 있어요.\","
                    + "\"fire\":\"행정동의 화재 건수가 상위 25%로 많은 편이에요. 도로접면은 폭 8m 미만으로 좁고, 가까운 소방서는 약 540m로 골든타임 내 출동이 가능해요.\","
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

    /**
     * 5문장 생성. 호출 자체 실패나 JSON 파싱 실패는 LlmCallFailedException으로 전체 폴백을 유발하지만,
     * 필드 하나의 검증(길이·어조·화재 표기 등) 실패는 그 필드만 폴백 문구로 대체하고 나머지 4개는
     * 그대로 반환한다 — {@link #isValidSentence}
     */
    public BasicBriefingDto generate(BriefingInput input) {
        String content = openAiClient.completeJson(SYSTEM_PROMPT, BriefingFactFormatter.buildUserPrompt(input));

        JsonNode root;
        try {
            root = objectMapper.readTree(content);
        } catch (Exception e) {
            throw new LlmCallFailedException("브리핑 JSON 파싱 실패: " + content, e);
        }

        String overallRaw = root.path("overall").asText(null);
        boolean overallValid = isValidSentence("overall", overallRaw, input.getTotalGrade());
        String overall = overallValid ? overallRaw : totalFallbackLead(input.getTotalGrade());

        return BasicBriefingDto.builder()
                .totalBrief(overall + " " + closingPhrase(input, overallValid))
                .structBrief(factorSentence("structure", "구조", input.getStructFacts(), root, input.getStructGrade()))
                .fireBrief(factorSentence("fire", "화재", input.getFireFacts(), root, input.getFireGrade()))
                .sinkBrief(factorSentence("sinkhole", "지반침하", input.getSinkFacts(), root, input.getSinkGrade()))
                .floodBrief(factorSentence("flood", "침수", input.getFloodFacts(), root, input.getFloodGrade()))
                .build();
    }

    /**
     * 사실이 "정보 없음"뿐인 팩터는 LLM 문장을 버리고 고정 문장으로 대체 — 프롬프트 예시나
     * 다른 항목의 사실이 섞여 들어오는 환각을 코드 레벨에서 차단한다 (실측: 구조 결측 시
     * 예시 출력의 "철근콘크리트"를 끌어와 모순 문장을 생성)
     */
    private String factorSentence(String key, String label, String facts, JsonNode root, SafetyGrade grade) {
        if (BriefingInput.NO_FACTS.equals(facts)) {
            return label + " 관련 정보는 아직 확인되지 않았어요.";
        }
        String sentence = root.path(key).asText(null);
        if (isValidSentence(key, sentence, grade)) {
            return sentence;
        }
        return factorFallbackSentence(label, grade);
    }

    /**
     * 필드 하나가 검증에 실패해도 예외를 전파해 5개 문장을 통째로 버리지 않고, 그 필드만 로그를
     * 남기고 false를 반환해 호출부가 그 필드만 폴백 문구로 대체하게 한다 — 실측상 필드 1개 위반으로
     * 나머지 4개 정상 문장까지 전부 버려지는 게 폴백률을 부풀리는 주된 원인이었다.
     * JSON 파싱 자체가 깨진 경우는 필드 단위로 복구할 데이터가 없어 이 경로를 타지 않고
     * generate()에서 그대로 전체 예외로 던진다.
     */
    private boolean isValidSentence(String key, String sentence, SafetyGrade grade) {
        try {
            validateSentence(key, sentence);
            BriefingToneValidator.check(key, sentence, grade);
            return true;
        } catch (LlmCallFailedException e) {
            log.warn("브리핑 필드 검증 실패, 해당 필드만 폴백 문구로 대체: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 종합 마무리 문구 — LLM에 맡기지 않고 코드에서 확정해 뒤에 붙인다 (등급 판정은 룰 엔진
     * 소관이라는 원칙 + 마무리 표현 누락·변형이 반복된 실측 대응).
     * 정보 없음 팩터가 있으면 안심 표현을 한 단계 보수적으로 내리고 미확인 단서를 덧붙인다
     */
    String closingPhrase(BriefingInput input, boolean afterOverall) {
        List<String> missing = missingFactorLabels(input);
        String phrase;
        if (input.getTotalGrade() == SafetyGrade.CAUTION) {
            phrase = "꼼꼼히 확인해 보시면 좋겠어요.";
        } else if (input.getTotalGrade() == SafetyGrade.GOOD) {
            phrase = afterOverall ? "그 점 외에는 전반적으로 양호한 편이에요." : "전반적으로 양호한 편이에요.";
        } else if (missing.isEmpty()) {
            phrase = afterOverall ? "전반적으로는 안심하고 검토하셔도 좋아요." : "안심하고 검토하셔도 좋아요.";
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
    public BasicBriefingDto fallback(BriefingInput input) {
        return BasicBriefingDto.builder()
                .totalBrief(totalFallbackLead(input.getTotalGrade()) + " " + closingPhrase(input, false))
                .structBrief(factorFallbackSentence("구조", input.getStructGrade()))
                .fireBrief(factorFallbackSentence("화재", input.getFireGrade()))
                .sinkBrief(factorFallbackSentence("지반침하", input.getSinkGrade()))
                .floodBrief(factorFallbackSentence("침수", input.getFloodGrade()))
                .build();
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
        if ("overall".equals(key)) {
            checkNoFireCountInOverall(sentence);
        }
        if (sentence.length() < BRIEF_MIN_LENGTH || sentence.length() > BRIEF_MAX_LENGTH) {
            throw new LlmCallFailedException("브리핑 길이 이탈(" + key + ", " + sentence.length() + "자): " + sentence);
        }
    }

    /**
     * overall엔 화재 건수·분위 수치를 넣지 말라는 프롬프트 규칙이 few-shot 보강만으로는 계속 새어
     * (실측: "화재 항목이 행정동 상위 편으로 화재 건수가 많아 보이지만 다른 항목의 상태와 비교해 확인이
     * 필요한 건물이에요"처럼 등급 비교 규칙과 뒤섞여 앞뒤가 안 맞는 문장이 됨) 코드 안전망을 둔다.
     * BriefingToneValidator와 동일하게 프롬프트 재조정 대신 코드에서 걸러 폴백으로 보낸다.
     */
    private void checkNoFireCountInOverall(String overall) {
        if (!overall.contains("화재")) {
            return;
        }
        boolean mentionsCountOrQuartile = overall.contains("상위") || overall.contains("하위")
                || overall.contains("분위") || overall.contains("%");
        if (mentionsCountOrQuartile) {
            throw new LlmCallFailedException("overall에 화재 건수·분위 언급: " + overall);
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
