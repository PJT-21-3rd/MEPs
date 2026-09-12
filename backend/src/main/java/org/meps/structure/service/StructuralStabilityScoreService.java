package org.meps.structure.service;

import lombok.RequiredArgsConstructor;
import org.meps.structure.dto.BuildingStructuralInfoDto;
import org.meps.structure.dto.StructuralFactorDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;
import org.meps.structure.mapper.StructureMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 구조안정성 점수 계산.
 *
 * 산식: score = max(70, 100 - Σdeduction)
 * 4개 요소(사용승인일/주구조/위반건축물/지하층)를 서로 독립적으로 감점한 뒤 100점에서 차감한다.
 * 요소별 최대 감점 합(15+12+8+3=38)이 클램프 폭(30)보다 커서 고위험 조건이 겹치는 건물은 70점으로 수렴하도록 한다.
 */
@Service
@RequiredArgsConstructor
public class StructuralStabilityScoreService {

    private static final int MAX_SCORE = 100;
    private static final int MIN_SCORE = 70;

    // 내진설계 기준 강화 시점(건축법 시행령 개정)을 경계로 4구간 감점
    private static final LocalDate SEISMIC_1988 = LocalDate.of(1988, 6, 1);
    private static final LocalDate SEISMIC_2005 = LocalDate.of(2005, 7, 1);
    private static final LocalDate SEISMIC_2017 = LocalDate.of(2017, 2, 4);
    private static final DateTimeFormatter USE_APR_DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    private static final int DEDUCT_PRE_1988 = 15;
    private static final int DEDUCT_1988_2005 = 10;
    private static final int DEDUCT_2005_2017 = 5;
    private static final int DEDUCT_POST_2017 = 0;

    // use_apr_day가 NULL/길이 이상/"11111111" 같은 더미값인 건물이 실측상 0.2% 존재.
    // 결측을 최악 구간으로 오분류하지 않도록 감점 없이 별도 처리한다.
    private static final int DEDUCT_UNKNOWN_DATE = 0;

    private static final int DEDUCT_STEEL_RC = 0;
    private static final int DEDUCT_MASONRY = 12;
    private static final int DEDUCT_WOOD = 6;
    private static final int DEDUCT_UNCLASSIFIED = 8;
    private static final Map<String, Integer> STRUCTURE_DEDUCTIONS = buildStructureDeductions();

    private static final int DEDUCT_VIOLATION = 8;
    private static final int DEDUCT_UNDERGROUND = 3;

    private final StructureMapper structureMapper;

    public StructuralStabilityScoreResultDto getStructuralStabilityScore(String buildingId) {
        BuildingStructuralInfoDto info = structureMapper.findStructuralInfo(buildingId);
        return calculateScore(info);
    }

    public StructuralStabilityScoreResultDto calculateScore(BuildingStructuralInfoDto info) {
        List<StructuralFactorDto> factors = new ArrayList<>();
        factors.add(evaluateUseAprDay(info.getUseAprDay()));
        factors.add(evaluateStructureType(info.getStrctCdNm()));
        factors.add(evaluateViolation(info.getViolBdYn()));
        factors.add(evaluateUndergroundFloor(info.getUgrndFlr()));

        int totalDeduction = factors.stream().mapToInt(StructuralFactorDto::getDeduction).sum();
        int score = Math.max(MIN_SCORE, MAX_SCORE - totalDeduction);

        return StructuralStabilityScoreResultDto.of(score, factors);
    }

    private StructuralFactorDto evaluateUseAprDay(String useAprDay) {
        LocalDate parsed = parseUseAprDay(useAprDay);
        if (parsed == null) {
            return StructuralFactorDto.builder()
                    .factor("USE_APR_DAY")
                    .detail(useAprDay == null ? "정보 없음" : useAprDay)
                    .deduction(DEDUCT_UNKNOWN_DATE)
                    .reason("사용승인일 정보가 없거나 형식이 올바르지 않아 심사에서 제외")
                    .build();
        }

        int deduction;
        String reason;
        if (parsed.isBefore(SEISMIC_1988)) {
            deduction = DEDUCT_PRE_1988;
            reason = "1988년 내진설계 의무화 이전 사용승인 건물로 내진 설계 미적용 가능성이 높음";
        } else if (parsed.isBefore(SEISMIC_2005)) {
            deduction = DEDUCT_1988_2005;
            reason = "내진설계 의무화 초기(1988~2005) 기준 적용 건물로 적용 대상·기준이 제한적이었음";
        } else if (parsed.isBefore(SEISMIC_2017)) {
            deduction = DEDUCT_2005_2017;
            reason = "2005년 강화 기준 적용 건물이나 2017년 전면 확대 기준 이전";
        } else {
            deduction = DEDUCT_POST_2017;
            reason = "2017년 전면 확대된 내진설계 기준 적용 건물";
        }

        return StructuralFactorDto.builder()
                .factor("USE_APR_DAY")
                .detail(parsed.toString())
                .deduction(deduction)
                .reason(reason)
                .build();
    }

    private LocalDate parseUseAprDay(String useAprDay) {
        if (useAprDay == null || useAprDay.length() != 8) {
            return null;
        }
        try {
            LocalDate date = LocalDate.parse(useAprDay, USE_APR_DAY_FMT);
            if (date.getYear() < 1900) {
                return null;
            }
            return date;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private StructuralFactorDto evaluateStructureType(String strctCdNm) {
        if (strctCdNm == null) {
            return StructuralFactorDto.builder()
                    .factor("STRUCTURE_TYPE")
                    .detail("정보 없음")
                    .deduction(DEDUCT_UNCLASSIFIED)
                    .reason("주구조 정보가 없어 안전성을 확인할 수 없음")
                    .build();
        }

        int deduction = STRUCTURE_DEDUCTIONS.getOrDefault(strctCdNm, DEDUCT_UNCLASSIFIED);

        return StructuralFactorDto.builder()
                .factor("STRUCTURE_TYPE")
                .detail(strctCdNm)
                .deduction(deduction)
                .reason(structureReason(deduction))
                .build();
    }

    private String structureReason(int deduction) {
        if (deduction == DEDUCT_MASONRY) {
            return "비보강 조적조 계열로 내진 성능이 낮아 최대 감점 적용";
        }
        if (deduction == DEDUCT_WOOD) {
            return "목구조로 내화·내진 특성이 RC/철골 대비 낮음";
        }
        if (deduction == DEDUCT_STEEL_RC) {
            return "철근콘크리트/철골 계열로 내진 성능이 검증된 구조";
        }
        return "표준코드에 없는 주구조 값이거나 재료 특성이 불명확하여 보수적으로 평가";
    }

    private StructuralFactorDto evaluateViolation(String violBdYn) {
        boolean violated = "Y".equals(violBdYn);
        return StructuralFactorDto.builder()
                .factor("VIOLATION")
                .detail(violBdYn == null ? "N" : violBdYn)
                .deduction(violated ? DEDUCT_VIOLATION : 0)
                .reason(violated
                        ? "위반건축물로 등재됨 — 구조·용도 무단 변경 등 불법 사항 존재 가능성"
                        : "위반건축물 이력 없음")
                .build();
    }

    private StructuralFactorDto evaluateUndergroundFloor(Integer ugrndFlr) {
        boolean hasBasement = ugrndFlr != null && ugrndFlr > 0;
        return StructuralFactorDto.builder()
                .factor("UNDERGROUND_FLOOR")
                .detail(ugrndFlr == null ? "정보 없음" : "지하 " + ugrndFlr + "층")
                .deduction(hasBasement ? DEDUCT_UNDERGROUND : 0)
                .reason(hasBasement
                        ? "지하층이 있어 방수·토압에 취약한 요소 존재"
                        : "지하층 없음")
                .build();
    }

    private static Map<String, Integer> buildStructureDeductions() {
        Map<String, Integer> deductions = new HashMap<>();

        // RC/SRC/철골 계열: 내진 성능 검증됨, 감점 없음
        for (String code : new String[]{
                "철근콘크리트구조", "철골철근콘크리트구조", "일반철골구조", "철골콘크리트구조",
                "경량철골구조", "프리케스트콘크리트구조", "철골철근콘크리트합성구조",
                "강파이프구조", "기타강구조", "보강콘크리트조", "기타콘크리트구조", "라멘조"
        }) {
            deductions.put(code, DEDUCT_STEEL_RC);
        }

        // 조적조 계열: 최대 감점
        for (String code : new String[]{"벽돌구조", "블록구조", "시멘트블럭조", "기타조적구조", "석구조"}) {
            deductions.put(code, DEDUCT_MASONRY);
        }

        // 목구조
        deductions.put("일반목구조", DEDUCT_WOOD);

        // 기타/미분류: 표준코드 상 존재하나 재료 특성이 불명확
        for (String code : new String[]{"기타구조", "조립식판넬조"}) {
            deductions.put(code, DEDUCT_UNCLASSIFIED);
        }

        return Collections.unmodifiableMap(deductions);
    }
}
