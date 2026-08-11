package org.meps.insurance.data;

/** 진단 요소 코드. 맞춤 특약 조회 factors 파라미터 검증용 */
public enum RiskFactor {
    STRUCTURE,
    FIRE,
    SINKHOLE,
    FLOOD;

    /** 유효하지 않은 코드면 IllegalArgumentException (서비스에서 InvalidFactorException으로 변환) */
    public static RiskFactor from(String code) {
        return RiskFactor.valueOf(code.trim().toUpperCase());
    }
}