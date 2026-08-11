package org.meps.loan.data;

public enum LoanType {
    STARTUP("창업자금"),
    CREDIT("신용대출"),
    GUARANTEE("보증서대출"),
    REFINANCE("대환대출");

    private final String label;

    LoanType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
