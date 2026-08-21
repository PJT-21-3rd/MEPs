package org.meps.safetyreport.service;

import org.meps.common.llm.LlmCallFailedException;
import org.meps.common.util.SafetyGrade;

import java.util.List;

/**
 * LLM이 쓴 문장의 어조가 룰 엔진이 확정한 등급과 모순되지 않는지 검증.
 * 프롬프트 규칙만으로는 안심 어휘 쪽이 동의어로 계속 우회되어(안정적→양호한 편→...) 안전망으로 코드 검증을 둔다.
 */
final class BriefingToneValidator {
    private static final List<String> WARN_WORDS = List.of(
            "우려", "불안", "취약", "위협", "주의가 필요", "주의를 요", "조심하", "걱정되");
    private static final List<String> REASSURE_WORDS = List.of(
            "안심", "걱정 없", "걱정하지 않", "문제없", "양호한 편", "안전한 편",
            "특이 사항 없", "특이사항 없", "안정적", "견고", "튼튼");

    private BriefingToneValidator() {
    }

    static void check(String key, String text, SafetyGrade grade) {
        List<String> banned = grade == SafetyGrade.CAUTION ? REASSURE_WORDS : WARN_WORDS;
        for (String word : banned) {
            if (text.contains(word)) {
                throw new LlmCallFailedException(
                        "등급-어조 불일치(" + key + ", 등급=" + grade.getLabel() + "): '" + word + "' 포함 - " + text);
            }
        }
    }
}
