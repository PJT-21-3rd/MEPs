package org.meps.loan.dto;

import lombok.*;

import java.util.List;

@Builder
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LoanProductResponseDto {
    private List<LoanProductItemDto> items;
}
