package org.meps.sinkhole.dto;

/**
 * 요소 배지·종합 등급 공용 구간표: 70~79 주의 / 80~89 양호 / 90~100 안전.
 * 점수는 사용자에게 비공개, 등급만 노출한다. DB에는 점수만 저장하고 등급은 여기서 파생한다.
 */
public enum SafetyGrade {
    SAFE("안전"),
    GOOD("양호"),
    CAUTION("주의");

    private final String label;

    SafetyGrade(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static SafetyGrade fromScore(int score) {
        if (score >= 90) {
            return SAFE;
        }
        if (score >= 80) {
            return GOOD;
        }
        return CAUTION;
    }
}
