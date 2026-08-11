package org.meps.loan.data;

/**
 * 대출 상품 도메인 모델
 * 현재는 하드코딩 데이터지만, 추후 MyBatis resultMap 생성자 매핑으로 DB 연동 예정.
 */
public record LoanProduct(
        LoanType loanType,
        String loanName,
        String coverageSummary,
        float minRate,
        int maxLimit
) {
}