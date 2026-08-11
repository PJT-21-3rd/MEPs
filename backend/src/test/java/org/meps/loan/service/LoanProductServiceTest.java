package org.meps.loan.service;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.loan.dto.LoanProductItemDto;
import org.meps.loan.dto.LoanProductResponseDto;

import static org.assertj.core.api.Assertions.assertThat;

class LoanProductServiceTest {
    private final LoanService loanService = new LoanService();

    @Test
    @DisplayName("대출 상품 목록을 정상적으로 반환한다")
    void getLoanProducts_returnsAllProducts() {
        // when
        LoanProductResponseDto response = loanService.getLoanProducts();

        // then
        assertThat(response.getItems()).hasSize(4);
    }

    @Test
    @DisplayName("loanType이 enum이 아닌 한글 라벨로 변환되어 내려간다")
    void getLoanProducts_convertsLoanTypeToLabel() {
        // when
        LoanProductResponseDto response = loanService.getLoanProducts();
        LoanProductItemDto startupLoan = response.getItems().get(0);

        // then
        assertThat(startupLoan.getLoanType()).isEqualTo("창업자금");
    }

    @Test
    @DisplayName("KB사장님+ 마이너스통장 상품의 필드가 정확히 매핑된다")
    void getLoanProducts_mapsStartupLoanFieldsCorrectly() {
        // when
        LoanProductResponseDto response = loanService.getLoanProducts();
        LoanProductItemDto startupLoan = response.getItems().get(0);

        // then
        assertThat(startupLoan.getLoanName()).isEqualTo("KB사장님+ 마이너스통장");
        assertThat(startupLoan.getCoverageSummary())
                .isEqualTo("사장님, 필요한만큼 사용하고 언제든 상환하세요");
        assertThat(startupLoan.getMinRate()).isEqualTo(3.6f);
        assertThat(startupLoan.getMaxLimit()).isEqualTo(100_000_000);
    }
}
