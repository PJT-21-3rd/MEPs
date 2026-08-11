package org.meps.insurance.service;

import org.meps.insurance.data.InsuranceDetail;
import org.meps.insurance.data.RiskFactor;
import org.meps.insurance.dto.InsuranceItemDto;
import org.meps.insurance.dto.InsuranceResponseDto;
import org.meps.insurance.exception.InvalidFactorException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class InsuranceService {

    /**
     * 주의(CAUTION) 판정 팩터 목록 → 요소별 보험 안내 항목 조회.
     * factors=A,B (쉼표 구분)와 factors=A&factors=B (반복) 모두 허용.
     * 요청 순서 유지, 중복 팩터는 최초 1회만 반영, 매핑 없는 코드는 items에서 제외.
     */
    public InsuranceResponseDto getInsuranceDetails(List<String> rawFactors) {
        Set<RiskFactor> factors = parse(rawFactors);

        List<InsuranceItemDto> items = new ArrayList<>();
        for (RiskFactor factor : factors) {
            for (InsuranceDetail detail : InsuranceDetail.findByFactor(factor)) {
                items.add(InsuranceItemDto.from(detail));
            }
        }
        return InsuranceResponseDto.builder().items(items).build();
    }

    /** 쉼표 분리 + trim + 코드 검증. LinkedHashSet으로 순서 보존·중복 제거 */
    private Set<RiskFactor> parse(List<String> rawFactors) {
        if (rawFactors == null || rawFactors.isEmpty()) {
            throw new InvalidFactorException("factors 파라미터가 비어 있습니다.");
        }
        Set<RiskFactor> result = new LinkedHashSet<>();
        for (String raw : rawFactors) {
            if (raw == null) {
                continue;
            }
            int start = 0;
            while (true) {
                int comma = raw.indexOf(',', start);
                int end = (comma == -1) ? raw.length() : comma;
                String code = raw.substring(start, end).trim();
                if (!code.isEmpty()) {
                    try {
                        result.add(RiskFactor.from(code));
                    } catch (IllegalArgumentException e) {
                        throw new InvalidFactorException("유효하지 않은 진단 요소 코드: " + code);
                    }
                }
                if (comma == -1) {
                    break;
                }
                start = comma + 1;
            }
        }
        if (result.isEmpty()) {
            throw new InvalidFactorException("factors 파라미터가 공백입니다.");
        }
        return result;
    }
}