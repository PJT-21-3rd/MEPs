package org.meps.structure.dto;

import lombok.*;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StructuralFactorDto {
    private String factor;    // USE_APR_DAY / STRUCTURE_TYPE / VIOLATION / UNDERGROUND_FLOOR
    private String detail;    // 판정에 사용된 원본/정규화 값
    private int deduction;    // 이 요소로 인한 감점(0 이상)
    private String reason;    // AI 브리핑 생성용 사유
}
