package org.meps.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Builder
@Setter @Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceResponseDto {
    private List<InsuranceItemDto> items;
}