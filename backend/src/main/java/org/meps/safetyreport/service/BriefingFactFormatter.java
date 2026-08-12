package org.meps.safetyreport.service;

import org.meps.common.util.SafetyGrade;
import org.meps.safetyreport.dto.BriefingInput;

/**
 * BriefingInput(룰 엔진 판정 결과)을 LLM 프롬프트용 사실 나열 텍스트로 조립.
 */
final class BriefingFactFormatter {
    private BriefingFactFormatter() {
    }

    static String buildUserPrompt(BriefingInput input) {
        StringBuilder sb = new StringBuilder();
        sb.append("[종합] 등급: ").append(input.getTotalGrade().getLabel()).append('\n');
        sb.append(factorLine("구조", input.getStructGrade(), input.getStructFacts())).append('\n');
        sb.append(factorLine("화재", input.getFireGrade(), input.getFireFacts())).append('\n');
        sb.append(factorLine("지반침하", input.getSinkGrade(), input.getSinkFacts())).append('\n');
        sb.append(factorLine("침수", input.getFloodGrade(), input.getFloodFacts()));
        return sb.toString();
    }

    /**
     * 정보 없음 팩터는 등급 없이 보냄
     */
    private static String factorLine(String label, SafetyGrade grade, String facts) {
        if (BriefingInput.NO_FACTS.equals(facts)) {
            return "[" + label + "] " + BriefingInput.NO_FACTS;
        }
        return "[" + label + "] 등급: " + grade.getLabel() + " / " + facts;
    }
}
