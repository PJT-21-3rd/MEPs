package org.meps.insurance.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 진단 팩터 → 보험 안내 항목 매핑.
 * 근거: KB 사업장종합보험(무배당)(26.01) 약관 + 풍수해·지진재해보험 VI
 * 팩터별 표시 순서 = enum 선언 순서 (findByFactor가 선언 순서를 보존).
 * coverageSummary는 약관 원문 대조 완료
 */
public enum InsuranceDetail {

    // FLOOD: 침수 이력 팩터
    FLOOD_STORM_DAMAGE(RiskFactor.FLOOD, CoverageType.RIDER, "풍수재손해",
            "태풍·홍수·해일 등 풍재·수재로 인한 사업장 재물 손해를 보장하는 유형입니다. (노후·하자로 인한 손해는 보상 제외)"),
    FLOOD_POLICY_INSURANCE(RiskFactor.FLOOD, CoverageType.PRODUCT, "풍수해·지진재해보험(VI)",
            "풍수해·지진으로 인한 재물 손해를 보장하는 정책보험으로, 정부·지자체가 보험료의 55% 이상을 지원합니다."),

    // STRUCTURE: 구조취약도 팩터
    STRUCTURE_WATER_LEAK(RiskFactor.STRUCTURE, CoverageType.RIDER, "급배수시설누출손해",
            "급배수시설 누수로 인한 재물 손해를 보장하는 유형입니다. (보장개시일 계약일+90일, 자기부담금 10%)"),

    // SINKHOLE: 지반침하 팩터
    // - 특약 3 제1조: 붕괴·침강 = 내부결함·부식·침식 등으로 갑자기 무너지거나 내려앉는 것 / 사태 = 비로 인한 토사 붕괴
    // - 특약 3 제2조 7호: 지진·분화로 인한 손해 면책
    SINKHOLE_COLLAPSE(RiskFactor.SINKHOLE, CoverageType.RIDER, "붕괴·침강 및 사태",
            "건물·건축구조물의 내부결함, 부식·침식 등으로 인한 붕괴·침강과 비로 인한 사태로 발생한 사업장 재물 손해를 보장하는 유형입니다. (지진으로 인한 손해는 보상 제외)"),

    // FIRE: 화재이력 팩터 (보통약관 + 보조 특약 2종)
    // - 제1절 제1조: 화재는 벼락 포함 / 제2절 화재손해 제1조: 직접손해·소방손해·피난손해 / 제2조 7호: 지진 등 기인 화재 면책
    FIRE_BASE(RiskFactor.FIRE, CoverageType.BASE, "화재손해",
            "화재(벼락 포함)로 인한 직접손해와 소방·피난 과정의 손해 등 사업장 재물 손해를 보장하는 보통약관 기본 보장입니다. (지진 등으로 생긴 화재는 보상 제외)");

    private final RiskFactor factor;
    private final CoverageType coverageType;
    private final String name;
    private final String coverageSummary;

    InsuranceDetail(RiskFactor factor, CoverageType coverageType, String name, String coverageSummary) {
        this.factor = factor;
        this.coverageType = coverageType;
        this.name = name;
        this.coverageSummary = coverageSummary;
    }

    public RiskFactor getFactor() {
        return factor;
    }

    public CoverageType getCoverageType() {
        return coverageType;
    }

    public String getName() {
        return name;
    }

    public String getCoverageSummary() {
        return coverageSummary;
    }

    /** 해당 팩터의 안내 항목을 선언 순서대로 반환 (매핑 없으면 빈 리스트) */
    public static List<InsuranceDetail> findByFactor(RiskFactor factor) {
        List<InsuranceDetail> result = new ArrayList<>();
        for (InsuranceDetail detail : values()) {
            if (detail.factor == factor) {
                result.add(detail);
            }
        }
        return result;
    }
}