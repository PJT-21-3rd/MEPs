package org.meps.safetyreport.dto;

import lombok.*;

/** AI 핵심 브리핑 5문장 (종합 + 구조/화재/지반침하/침수). LLM 생성 결과, DB에 저장해 둔 값, 또는 폴백 값 */
@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BasicBriefingDto {
    private String totalBrief;
    private String structBrief;
    private String fireBrief;
    private String sinkBrief;
    private String floodBrief;
}
