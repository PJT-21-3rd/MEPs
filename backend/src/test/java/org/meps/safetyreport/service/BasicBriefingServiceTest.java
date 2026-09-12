package org.meps.safetyreport.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.common.llm.OpenAiClient;
import org.meps.common.util.SafetyGrade;
import org.meps.safetyreport.dto.BriefingInput;
import org.meps.safetyreport.dto.BasicBriefingDto;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BasicBriefingServiceTest {

    /** 고정 응답을 돌려주는 OpenAiClient 스텁 (Mockito 미사용 프로젝트라 서브클래스로 대체) */
    private static OpenAiClient stubClient(String cannedContent) {
        return new OpenAiClient(null, null, null) {
            @Override
            public String completeJson(String systemPrompt, String userPrompt) {
                return cannedContent;
            }
        };
    }

    private static BasicBriefingService serviceWith(String cannedContent) {
        return new BasicBriefingService(stubClient(cannedContent), new ObjectMapper());
    }

    private static BriefingInput input() {
        return BriefingInput.builder()
                .totalGrade(SafetyGrade.GOOD)
                .structGrade(SafetyGrade.SAFE)
                .structFacts("정보 없음")
                .fireGrade(SafetyGrade.CAUTION)
                .fireFacts("주구조: 일반목구조 / 도로접면: 맹지 / 최근접 소방서: 강남소방서 2100m / 행정동 최근 3년 평균 화재: 20.0건(서울 행정동 중 상위 25%)")
                .sinkGrade(SafetyGrade.SAFE)
                .sinkFacts("반경 500m 내 지반침하 사고 이력: 없음")
                .floodGrade(SafetyGrade.SAFE)
                .floodFacts("정보 없음")
                .build();
    }

    private static final String VALID_JSON = "{"
            + "\"overall\":\"전반적으로 양호하나 화재 항목에서 확인이 필요한 조건이 있는 건물입니다.\","
            + "\"structure\":\"구조 관련 상세 정보는 확인되지 않는 건물입니다.\","
            + "\"fire\":\"목구조에 소방차 진입이 어려운 접면 조건이 확인되는 건물입니다.\","
            + "\"sinkhole\":\"반경 500m 이내 지반침하 사고 이력이 확인되지 않았습니다.\","
            + "\"flood\":\"침수 관련 상세 정보는 확인되지 않는 건물입니다.\"}";

    @Test
    void 정상_JSON이면_5문장이_DTO로_매핑된다() {
        BasicBriefingDto dto = serviceWith(VALID_JSON).generate(input());

        assertThat(dto.getTotalBrief()).contains("양호");
        // 마무리 문구는 LLM이 아니라 코드가 붙인다 — 종합 GOOD + 구조·침수 정보 없음
        assertThat(dto.getTotalBrief())
                .endsWith("그 점 외에는 전반적으로 양호한 편이에요. 다만 구조·침수 정보는 아직 확인되지 않았어요.");
        assertThat(dto.getStructBrief()).contains("구조");
        assertThat(dto.getFireBrief()).contains("목구조");
        assertThat(dto.getSinkBrief()).contains("지반침하");
        assertThat(dto.getFloodBrief()).contains("침수");
    }

    @Test
    void 사실이_있는_팩터의_키가_누락되면_그_필드만_폴백_문구로_대체된다() {
        String missingSinkhole = "{"
                + "\"overall\":\"전반적으로 양호한 상태가 확인되는 건물입니다.\","
                + "\"structure\":\"구조 관련 상세 정보는 확인되지 않는 건물입니다.\","
                + "\"fire\":\"목구조에 소방차 진입이 어려운 접면 조건이 확인되는 건물입니다.\","
                + "\"flood\":\"침수 관련 상세 정보는 확인되지 않는 건물입니다.\"}";

        BasicBriefingDto dto = serviceWith(missingSinkhole).generate(input());

        // sinkhole 키 누락은 그 필드만 폴백 문구로 대체되고, 나머지 4개는 LLM 문장을 그대로 쓴다
        assertThat(dto.getSinkBrief()).isEqualTo("지반침하 항목에서 특이 이력은 확인되지 않았어요.");
        assertThat(dto.getFireBrief()).isEqualTo("목구조에 소방차 진입이 어려운 접면 조건이 확인되는 건물입니다.");
    }

    @Test
    void 정보없음_팩터는_LLM_문장을_버리고_고정_문장으로_대체한다() {
        // VALID_JSON의 structure 문장 대신 고정 문장 — 예시·타 항목 사실이 섞이는 환각 차단 가드
        BasicBriefingDto dto = serviceWith(VALID_JSON).generate(input());

        assertThat(dto.getStructBrief()).isEqualTo("구조 관련 정보는 아직 확인되지 않았어요.");
        assertThat(dto.getFloodBrief()).isEqualTo("침수 관련 정보는 아직 확인되지 않았어요.");
    }

    @Test
    void JSON이_아니면_예외다() {
        assertThatThrownBy(() -> serviceWith("죄송합니다. 요약을 생성할 수 없습니다.").generate(input()))
                .isInstanceOf(LlmCallFailedException.class);
    }

    @Test
    void 문장이_너무_짧으면_그_필드만_폴백_문구로_대체된다() {
        String tooShort = VALID_JSON.replace(
                "반경 500m 이내 지반침하 사고 이력이 확인되지 않았습니다.", "양호함");

        BasicBriefingDto dto = serviceWith(tooShort).generate(input());

        assertThat(dto.getSinkBrief()).isEqualTo("지반침하 항목에서 특이 이력은 확인되지 않았어요.");
    }

    private static final String FALLBACK_TOTAL_BRIEF_GOOD_MISSING_STRUCT_FLOOD =
            "큰 특이 사항 없이 확인되는 건물이에요. 전반적으로 양호한 편이에요."
                    + " 다만 구조·침수 정보는 아직 확인되지 않았어요.";

    @Test
    void 화재_건수를_행정동_표기_없이_쓰면_overall만_폴백_문구로_대체된다() {
        // 행정동 통계가 건물 자체 화재 이력처럼 읽히는 문장을 걸러낸다
        String withoutSource = VALID_JSON.replace(
                "전반적으로 양호하나 화재 항목에서 확인이 필요한 조건이 있는 건물입니다.",
                "화재 건수가 다소 많고 소방차 진입이 어려운 조건이 있는 건물입니다.");

        BasicBriefingDto dto = serviceWith(withoutSource).generate(input());

        assertThat(dto.getTotalBrief()).isEqualTo(FALLBACK_TOTAL_BRIEF_GOOD_MISSING_STRUCT_FLOOD);
    }

    @Test
    @DisplayName("안전/양호 등급 문장에 경고 표현이 있으면 그 필드만 폴백 문구로 대체된다")
    void generate_warningWordInSafeOrGoodSentence_fallsBackToTemplate() {
        String withWarnWord = VALID_JSON.replace(
                "전반적으로 양호하나 화재 항목에서 확인이 필요한 조건이 있는 건물입니다.",
                "전반적으로 양호하나 화재 항목이 다소 우려되는 건물입니다.");

        BasicBriefingDto dto = serviceWith(withWarnWord).generate(input());

        assertThat(dto.getTotalBrief()).isEqualTo(FALLBACK_TOTAL_BRIEF_GOOD_MISSING_STRUCT_FLOOD);
        // overall이 걸려도 다른 필드는 영향받지 않는다
        assertThat(dto.getFireBrief()).isEqualTo("목구조에 소방차 진입이 어려운 접면 조건이 확인되는 건물입니다.");
    }

    @Test
    @DisplayName("주의 등급 문장에 안심 표현이 있으면 그 필드만 폴백 문구로 대체된다")
    void generate_reassureWordInCautionSentence_fallsBackToTemplate() {
        String withReassureWord = VALID_JSON.replace(
                "목구조에 소방차 진입이 어려운 접면 조건이 확인되는 건물입니다.",
                "목구조지만 비교적 안정적인 접면 조건이 확인되는 건물입니다.");

        BasicBriefingDto dto = serviceWith(withReassureWord).generate(input());

        assertThat(dto.getFireBrief()).isEqualTo("화재 항목에서 확인이 필요한 이력이 있어요.");
        // fire가 걸려도 overall 등 다른 필드는 영향받지 않는다
        assertThat(dto.getTotalBrief()).contains("양호");
    }

    @Test
    @DisplayName("overall에 화재 건수·분위 언급이 있으면 overall만 폴백 문구로 대체된다")
    void generate_fireCountInOverall_fallsBackToTemplate() {
        String withFireCountInOverall = VALID_JSON.replace(
                "전반적으로 양호하나 화재 항목에서 확인이 필요한 조건이 있는 건물입니다.",
                "화재 항목이 행정동 상위 편으로 화재 건수가 많아 보이지만 다른 항목의 상태와 비교해 확인이 필요한 건물이에요.");

        BasicBriefingDto dto = serviceWith(withFireCountInOverall).generate(input());

        assertThat(dto.getTotalBrief()).isEqualTo(FALLBACK_TOTAL_BRIEF_GOOD_MISSING_STRUCT_FLOOD);
    }

    @Test
    void 폴백은_등급별_고정_문장을_돌려준다() {
        BasicBriefingDto dto = serviceWith(VALID_JSON).fallback(input());

        // 종합 GOOD, 화재 CAUTION, 나머지 SAFE — 종합엔 마무리 문구와 미확인 단서까지 붙는다
        assertThat(dto.getTotalBrief()).isEqualTo(
                "큰 특이 사항 없이 확인되는 건물이에요. 전반적으로 양호한 편이에요."
                        + " 다만 구조·침수 정보는 아직 확인되지 않았어요.");
        assertThat(dto.getFireBrief()).isEqualTo("화재 항목에서 확인이 필요한 이력이 있어요.");
        assertThat(dto.getStructBrief()).isEqualTo("구조 항목에서 특이 이력은 확인되지 않았어요.");
        assertThat(dto.getSinkBrief()).isEqualTo("지반침하 항목에서 특이 이력은 확인되지 않았어요.");
        assertThat(dto.getFloodBrief()).isEqualTo("침수 항목에서 특이 이력은 확인되지 않았어요.");
    }

    @Test
    void 안전_등급도_정보없음이_있으면_안심_문구를_보수적으로_내린다() {
        BriefingInput safeWithMissing = input();
        safeWithMissing.setTotalGrade(SafetyGrade.SAFE);
        assertThat(serviceWith(VALID_JSON).closingPhrase(safeWithMissing, true)).isEqualTo(
                "확인된 항목들은 안정적인 편이에요. 다만 구조·침수 정보는 아직 확인되지 않았어요.");
    }

    @Test
    void 안전_등급이고_전_팩터_정보가_있으면_안심_문구를_쓴다() {
        BriefingInput allKnown = input();
        allKnown.setTotalGrade(SafetyGrade.SAFE);
        allKnown.setStructFacts("주구조: 철근콘크리트구조 / 사용승인: 1998년");
        allKnown.setFloodFacts("지번 침수 이력: 없음");

        assertThat(serviceWith(VALID_JSON).closingPhrase(allKnown, true))
                .isEqualTo("전반적으로는 안심하고 검토하셔도 좋아요.");
    }

    @Test
    void 폴백_경로는_지시어_없이_기존_안심_문구를_쓴다() {
        // fallback()의 리드 문장은 특정 사실을 가리키지 않는 범용 템플릿이라 "전반적으로는" 같은
        // 지시어를 붙이면 어색함 — generate() 경로(afterOverall=true)와 달리 원문구를 유지한다
        BriefingInput allKnown = input();
        allKnown.setTotalGrade(SafetyGrade.SAFE);
        allKnown.setStructFacts("주구조: 철근콘크리트구조 / 사용승인: 1998년");
        allKnown.setFloodFacts("지번 침수 이력: 없음");

        assertThat(serviceWith(VALID_JSON).closingPhrase(allKnown, false))
                .isEqualTo("안심하고 검토하셔도 좋아요.");
    }

    @Test
    void 프롬프트는_5개_팩터_라인에_등급_라벨과_사실을_담는다() {
        //String prompt = serviceWith(VALID_JSON).buildUserPrompt(input());
        String prompt = BriefingFactFormatter.buildUserPrompt(input());

        assertThat(prompt).startsWith("[종합] 등급: 양호");
        // 정보 없음 팩터는 등급 없이 — 모델이 "구조 안전"을 단정할 재료를 주지 않는다
        assertThat(prompt).contains("[구조] 정보 없음");
        assertThat(prompt).doesNotContain("[구조] 등급:");
        assertThat(prompt).contains("[화재] 등급: 주의 / 주구조: 일반목구조");
        assertThat(prompt).contains("[지반침하] 등급: 안전 / 반경 500m 내 지반침하 사고 이력: 없음");
        assertThat(prompt).contains("[침수] 정보 없음");
    }
}
