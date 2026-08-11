package org.meps.loan.service;

import org.meps.loan.data.LoanProduct;
import org.meps.loan.data.LoanType;
import org.meps.loan.dto.LoanProductItemDto;
import org.meps.loan.dto.LoanProductResponseDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {
    private static final List<LoanProduct> PRODUCTS = List.of(
            new LoanProduct(LoanType.STARTUP, "KB사장님+ 마이너스통장",
                    "사장님, 필요한만큼 사용하고 언제든 상환하세요",
                    3.6f, 100_000_000),

            new LoanProduct(LoanType.GUARANTEE, "KB소상공인 보증서대출(온택트)",
                    "사업장에서 모바일로 간편하게",
                    5.29f, 30_000_000),

            new LoanProduct(LoanType.CREDIT, "KB소상공인 신용대출",
                    "바쁜 개인사업자를 위한 비대면 신용대출",
                    3.63f, 200_000_000),

            new LoanProduct(LoanType.REFINANCE, "소상공인 저금리 대환대출(수탁보증)",
                    "정부 「자영업자·소상공인 대환프로그램」 전용상품으로, 고금리 기업대출(연 7% 이상)을 저금리 보증서대출로 전환할 수 있는 상품입니다.",
                    5.0f, 200_000_000)
    );

    public LoanProductResponseDto getLoanProducts() {
        List<LoanProductItemDto> items = PRODUCTS.stream()
                .map(LoanProductItemDto::from)
                .collect(Collectors.toList());
        return LoanProductResponseDto.builder().items(items).build();
    }
}
