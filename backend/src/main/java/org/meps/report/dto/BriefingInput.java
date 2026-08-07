package org.meps.report.dto;

import lombok.*;
import org.meps.common.util.SafetyGrade;

/**
 * LLM 브리핑 프롬프트 재료 — 팩터별 (등급, 사실 텍스트)만 담는 중립 홀더.
 * 점수 서비스 DTO에 직접 의존하지 않아 구조·침수 파트가 병합돼도 조립부만 바꾸면 된다.
 * 사실 텍스트는 "주구조: 철근콘크리트구조 / 도로접면: 광대로한면" 형태, 결측 팩터는 "정보 없음"
 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BriefingInput {
    /** 결측 팩터의 사실 표기. 이 값뿐인 팩터는 LLM을 거치지 않고 고정 문장으로 응답한다 */
    public static final String NO_FACTS = "정보 없음";

    private SafetyGrade totalGrade;
    private SafetyGrade structGrade;
    private String structFacts;
    private SafetyGrade fireGrade;
    private String fireFacts;
    private SafetyGrade sinkGrade;
    private String sinkFacts;
    private SafetyGrade floodGrade;
    private String floodFacts;
}
