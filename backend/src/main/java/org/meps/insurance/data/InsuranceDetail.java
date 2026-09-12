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
    FLOOD_STORM_DAMAGE(RiskFactor.FLOOD, CoverageType.RIDER, "[보험 특약] 풍수재손해",
            "태풍·홍수·해일 같은 바람과 물로 인한 사업장 재산 피해를 보상하는 사업장종합보험 특약이에요. (지진이나 화산 분화로 생긴 손해는 보상하지 않아요)"),
    FLOOD_POLICY_INSURANCE(RiskFactor.FLOOD, CoverageType.PRODUCT, "소상공인 풍수해·지진재해보험(Ⅵ)",
            "태풍·홍수·지진 같은 자연재해로 입은 사업장 재산 피해를 보상하는 정책보험이에요. 보험료의 일부를 국가와 지자체가 지원해줘요."),

    // STRUCTURE: 구조취약도 팩터
    STRUCTURE_WATER_LEAK(RiskFactor.STRUCTURE, CoverageType.RIDER, "[보험 특약] 급배수시설누출손해",
            "수도관·배수관 등에서 물이 새어 생긴 사업장 재산 피해를 보상하는 사업장종합보험 특약이에요. (가입 후 90일이 지나야 보장이 시작되고, 손해액의 10%는 본인이 부담해요)"),

    // SINKHOLE: 지반침하 팩터
    // - 특약 3 제1조: 붕괴·침강 = 내부결함·부식·침식 등으로 갑자기 무너지거나 내려앉는 것 / 사태 = 비로 인한 토사 붕괴
    // - 특약 3 제2조 7호: 지진·분화로 인한 손해 면책
    SINKHOLE_COLLAPSE(RiskFactor.SINKHOLE, CoverageType.RIDER, "[보험 특약] 붕괴·침강 및 사태로 인한 재산손해",
            "건물이 내부 결함이나 부식으로 갑자기 무너지거나 내려앉았을 때, 또는 비로 산사태가 났을 때 사업장 재산 피해를 보상하는 사업장종합보험 특약이에요. (지진으로 생긴 손해는 보상하지 않아요)"),

    // FIRE: 화재이력 팩터 (보통약관 + 보조 특약 2종)
    // - 제1절 제1조: 화재는 벼락 포함 / 제2절 화재손해 제1조: 직접손해·소방손해·피난손해 / 제2조 7호: 지진 등 기인 화재 면책
    FIRE_BASE(RiskFactor.FIRE, CoverageType.BASE, "[보험 기본보장] 화재손해",
            "불이 났을 때(벼락 포함), 직접 탄 피해뿐 아니라 불을 끄거나 피난하는 과정에서 생긴 사업장 재산 피해까지 보상하는 사업장종합보험의 기본 보장이에요. (지진 때문에 난 불은 보상하지 않아요)");

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