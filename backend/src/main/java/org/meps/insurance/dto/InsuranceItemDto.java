package org.meps.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.meps.insurance.data.InsuranceDetail;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceItemDto {
    private String factorCode;
    private String coverageType;
    private String name;
    private String coverageSummary;

    public static InsuranceItemDto from(InsuranceDetail detail) {
        return InsuranceItemDto.builder()
                .factorCode(detail.getFactor().name())
                .coverageType(detail.getCoverageType().name())
                .name(detail.getName())
                .coverageSummary(detail.getCoverageSummary())
                .build();
    }
}