package org.meps.report.service;

import lombok.RequiredArgsConstructor;
import org.meps.common.util.SafetyGrade;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.service.FireScoreService;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.service.FloodScoreService;
import org.meps.report.dto.BriefingInput;
import org.meps.report.dto.DetailedReportResponseDto;
import org.meps.report.dto.FactorReportDto;
import org.meps.report.dto.ReportDetailDto;
import org.meps.report.dto.SafetyBriefingDto;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.sinkhole.service.SinkholeScoreService;
import org.meps.structure.dto.StructuralFactorDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;
import org.meps.structure.service.StructuralStabilityScoreService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 안심 진단 상세 리포트
 *
 * details는 각 점수 서비스가 판정에 실제 사용한 값만 출처와 함께 노출
 * aiReport는 아직 등급별 템플릿 문장(BriefingService 폴백)으로 채움
 * 스키마, details를 먼저 확정하고, LLM 3단락 해석(근거→리스크→솔루션)은 후속 작업에서 교체
 */
@Service
@RequiredArgsConstructor
public class DetailedReportService {

    private static final String NO_INFO = BriefingInput.NO_FACTS;

    private final FireScoreService fireScoreService;
    private final SinkholeScoreService sinkholeScoreService;
    private final StructuralStabilityScoreService structuralStabilityScoreService;
    private final FloodScoreService floodScoreService;
    private final TotalScoreService totalScoreService;
    private final BriefingService briefingService;

    public DetailedReportResponseDto getDetailedReport(String buildingId) {
        FireScoreResult fire = fireScoreService.getFireScore(buildingId); // 미존재 건물이면 여기서 404
        SinkholeScoreResult sink = sinkholeScoreService.getSinkholeScore(buildingId);
        StructuralStabilityScoreResultDto struct = structuralStabilityScoreService.getStructuralStabilityScore(buildingId);
        FloodScoreResultDto flood = floodScoreService.getFloodScore(buildingId);

        int totalScore = totalScoreService.calculateTotalScore(
                struct.getScore(), fire.getScore(), sink.getScore(), flood.getScore());

        BriefingInput input = BriefingInput.builder()
                .totalGrade(SafetyGrade.fromScore(totalScore))
                .structGrade(struct.getGrade())
                .structFacts(SafetyReportService.buildStructFacts(struct))
                .fireGrade(fire.getGrade())
                .fireFacts(SafetyReportService.buildFireFacts(fire))
                .sinkGrade(sink.getGrade())
                .sinkFacts(SafetyReportService.buildSinkFacts(sink))
                .floodGrade(flood.getGrade())
                .floodFacts(SafetyReportService.buildFloodFacts(flood))
                .build();
        SafetyBriefingDto placeholder = briefingService.fallback(input);

        // factors 순서 고정: 구조 → 화재 → 지반침하 → 침수 (명세)
        List<FactorReportDto> factors = new ArrayList<>();
        factors.add(buildFactor("STRUCTURE", struct.getGrade(), placeholder.getStructBrief(), buildStructDetails(struct)));
        factors.add(buildFactor("FIRE", fire.getGrade(), placeholder.getFireBrief(), buildFireDetails(fire)));
        factors.add(buildFactor("SINKHOLE", sink.getGrade(), placeholder.getSinkBrief(), buildSinkDetails(sink)));
        factors.add(buildFactor("FLOOD", flood.getGrade(), placeholder.getFloodBrief(), buildFloodDetails(flood)));

        return DetailedReportResponseDto.builder()
                .overallAiReport(placeholder.getTotalBrief())
                .factors(factors)
                .build();
    }

    private FactorReportDto buildFactor(String code, SafetyGrade grade, String aiReport, List<ReportDetailDto> details) {
        return FactorReportDto.builder()
                .code(code)
                .status(grade.name())
                .aiReport(aiReport)
                .details(details)
                .build();
    }

    /** 구조 details */
    static List<ReportDetailDto> buildStructDetails(StructuralStabilityScoreResultDto struct) {
        List<ReportDetailDto> details = new ArrayList<>();
        details.add(buildDetail("주구조", findFactorDetail(struct, "STRUCTURE_TYPE"), "건축물대장 표제부"));
        details.add(buildDetail("사용승인일", findFactorDetail(struct, "USE_APR_DAY"), "건축물대장 표제부"));
        details.add(buildDetail("위반건축물 여부", violationValue(findFactorDetail(struct, "VIOLATION")), "건축물대장 표제부"));
        return details;
    }

    /** 화재 details */
    static List<ReportDetailDto> buildFireDetails(FireScoreResult fire) {
        List<ReportDetailDto> details = new ArrayList<>();
        details.add(buildDetail("도로접면", roadSideValue(fire.getRoadSideCodeNm()), "토지특성정보"));
        details.add(buildDetail("주구조", orNoInfo(fire.getStrctCdNm()), "건축물대장 표제부"));
        details.add(buildDetail("행정동 화재 건수(3년 평균)", fireCountValue(fire.getDongFireAvgCnt()), "소방청 화재통계"));
        details.add(buildDetail("최근접 소방서 거리", stationValue(fire), "소방서 위치정보"));
        return details;
    }

    /** 지반침하 details */
    static List<ReportDetailDto> buildSinkDetails(SinkholeScoreResult sink) {
        List<ReportDetailDto> details = new ArrayList<>();
        details.add(buildDetail("반경 내 사고 이력", sink.getIncidentCount() + "건", "지하안전정보 사고이력"));
        if (sink.getIncidentCount() > 0) {
            // 매퍼가 사고일 내림차순 정렬로 반환하므로 첫 건이 최근 사고
            SinkholeIncidentDto latest = sink.getIncidents().get(0);
            details.add(buildDetail("최근 사고",
                    SafetyReportService.formatSagoDate(latest.getSagoDate())
                            + ", 거리 " + Math.round(latest.getDistanceM()) + "m",
                    "지하안전정보 사고이력"));
        }
        return details;
    }

    /** 침수 details. 패키지 프라이빗 — 단위 테스트 대상 */
    static List<ReportDetailDto> buildFloodDetails(FloodScoreResultDto flood) {
        List<ReportDetailDto> details = new ArrayList<>();
        if (!flood.isFloodHistory()) {
            details.add(buildDetail("지번 침수 이력", "이력 없음", "행정안전부 침수흔적도"));
            details.add(buildDetail("침수위험등급", "해당 없음", "행정안전부 침수흔적도"));
            return details;
        }
        // 매퍼가 연도 내림차순 정렬로 반환하므로 첫 건이 최근 이력
        FloodIncidentDto latest = flood.getIncidents().get(0);
        details.add(buildDetail("지번 침수 이력",
                flood.getIncidentCount() + "건(최근 " + latest.getYear() + "년)", "행정안전부 침수흔적도"));
        details.add(buildDetail("침수위험등급",
                latest.getGrade() + "등급(" + latest.getYear() + "년)", "행정안전부 침수흔적도"));
        return details;
    }

    private static ReportDetailDto buildDetail(String label, String value, String source) {
        return ReportDetailDto.builder().label(label).value(value).source(source).build();
    }

    private static String findFactorDetail(StructuralStabilityScoreResultDto struct, String factorCode) {
        for (StructuralFactorDto factor : struct.getFactors()) {
            if (factor.getFactor().equals(factorCode)) {
                return factor.getDetail();
            }
        }
        return NO_INFO;
    }

    private static String violationValue(String raw) {
        if ("Y".equals(raw)) {
            return "있음";
        }
        if ("N".equals(raw)) {
            return "없음";
        }
        return raw; // "정보 없음" 등은 그대로 노출
    }

    /** "지정되지않음"은 토지특성 코드상 미지정 더미값 — 결측과 동일 취급(기본 리포트 사실 조립과 동일 규칙) */
    private static String roadSideValue(String roadSideCodeNm) {
        if (roadSideCodeNm == null || roadSideCodeNm.isBlank() || roadSideCodeNm.equals("지정되지않음")) {
            return NO_INFO;
        }
        return roadSideCodeNm;
    }

    private static String fireCountValue(Double dongFireAvgCnt) {
        if (dongFireAvgCnt == null) {
            return NO_INFO;
        }
        // SQL AVG의 원시 소수(19.3333…)가 그대로 노출되지 않게 소수 1자리로 반올림
        return Math.round(dongFireAvgCnt * 10.0) / 10.0 + "건";
    }

    private static String stationValue(FireScoreResult fire) {
        if (fire.getNearestStationNm() == null || fire.getNearestStationDistanceM() == null) {
            return NO_INFO;
        }
        return fire.getNearestStationNm() + " 약 " + Math.round(fire.getNearestStationDistanceM()) + "m";
    }

    private static String orNoInfo(String value) {
        if (value == null || value.isBlank()) {
            return NO_INFO;
        }
        return value;
    }
}
