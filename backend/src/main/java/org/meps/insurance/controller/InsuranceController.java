package org.meps.insurance.controller;

import lombok.RequiredArgsConstructor;
import org.meps.insurance.dto.InsuranceResponseDto;
import org.meps.insurance.service.InsuranceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/insurances")
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;

    /**
     * 맞춤 특약 조회 - 주의(CAUTION) 판정된 진단 요소 코드별 보험 안내 항목.
     */
    @GetMapping
    public InsuranceResponseDto getInsurances(@RequestParam("factors") List<String> factors) {
        return insuranceService.getInsuranceDetails(factors);
    }
}