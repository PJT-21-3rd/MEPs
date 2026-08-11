package org.meps.loan.dto;

import lombok.*;
import org.meps.loan.data.LoanProduct;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoanProductItemDto {

    private String loanType;
    private String loanName;
    private String coverageSummary;
    private Float minRate;
    private Integer maxLimit;

    public static LoanProductItemDto from(LoanProduct product) {
        return LoanProductItemDto.builder()
                .loanType(product.loanType().getLabel())
                .loanName(product.loanName())
                .coverageSummary(product.coverageSummary())
                .minRate(product.minRate())
                .maxLimit(product.maxLimit())
                .build();
    }
}
