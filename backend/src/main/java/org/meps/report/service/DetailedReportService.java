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
        details.add(buildDetail("지하층수", findFactorDetail(struct, "UNDERGROUND_FLOOR"), "건축물대장 표제부"));
        return details;
    }

    /** 화재 details */
    static List<ReportDetailDto> buildFireDetails(FireScoreResult fire) {
        List<ReportDetailDto> details = new ArrayList<>();
        details.add(buildDetail("도로접면", roadSideValue(fire.getRoadSideCodeNm()), "토지특성정보"));
        details.add(buildDetail("주구조", orNoInfo(fire.getStrctCdNm()), "건축물대장 표제부"));
        details.add(buildDetail("행정동 화재 건수(3년 평균)", fireCountValue(fire), "소방청 화재통계"));
        details.add(buildDetail("최근접 소방서 거리", stationValue(fire), "소방서 위치정보"));
        return details;
    }

    /**
     * 지반침하 details — 산식의 거리 가중 구간과 1:1 대응하는 3행 고정.
     * 사고 없는 구간도 "없음"으로 내보내 조회 범위(0~500m)를 명시한다
     */
    static List<ReportDetailDto> buildSinkDetails(SinkholeScoreResult sink) {
        String[] labels = {"0m~100m 사고 이력", "100m~300m 사고 이력", "300m~500m 사고 이력"};
        int[] counts = new int[labels.length];
        String[] latestSagoDates = new String[labels.length];
        for (SinkholeIncidentDto incident : sink.getIncidents()) {
            int band = calculateBandIndex(incident.getDistanceM());
            counts[band]++;
            if (latestSagoDates[band] == null) {
                // 매퍼가 사고일 내림차순 정렬로 반환하므로 구간 내 첫 매칭이 최근 사고
                latestSagoDates[band] = incident.getSagoDate();
            }
        }

        List<ReportDetailDto> details = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            String value;
            if (counts[i] == 0) {
                value = "없음";
            } else {
                value = counts[i] + "건(최근 " + SafetyReportService.formatSagoDate(latestSagoDates[i]) + ")";
            }
            details.add(buildDetail(labels[i], value, "지하안전정보 사고이력"));
        }
        return details;
    }

    /** 침수 details */
    static List<ReportDetailDto> buildFloodDetails(FloodScoreResultDto flood) {
        List<ReportDetailDto> details = new ArrayList<>();
        if (!flood.isFloodHistory()) {
            details.add(buildDetail("지번 침수 이력", "이력 없음", "행정안전부 침수흔적도"));
            details.add(buildDetail("최고 침수심 등급", "해당 없음", "행정안전부 침수흔적도"));
            return details;
        }
        // 매퍼가 연도 내림차순 정렬로 반환하므로 첫 건이 최근 이력이고,
        // 최고 등급이 여러 건이면 순회상 첫 매칭(최근 연도)을 대표로 쓴다
        FloodIncidentDto latest = flood.getIncidents().get(0);
        FloodIncidentDto worst = latest;
        for (FloodIncidentDto incident : flood.getIncidents()) {
            if (incident.getGrade() > worst.getGrade()) {
                worst = incident;
            }
        }
        details.add(buildDetail("지번 침수 이력",
                flood.getIncidentCount() + "건(최근 " + latest.getYear() + "년)", "행정안전부 침수흔적도"));
        details.add(buildDetail("최고 침수심 등급",
                worst.getGrade() + "등급(" + worst.getYear() + "년)", "행정안전부 침수흔적도"));
        return details;
    }

    /** 지반침하점수화 로직과 동일한 구간 경계로 사고를 분류한다 */
    private static int calculateBandIndex(double distanceM) {
        if (distanceM <= SinkholeScoreService.DIST_NEAR_M) {
            return 0;
        }
        if (distanceM <= SinkholeScoreService.DIST_MID_M) {
            return 1;
        }
        return 2; // 매핑 배치가 500m로 컷하므로 그 외 = 300~500m
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

    private static String fireCountValue(FireScoreResult fire) {
        if (fire.getDongFireAvgCnt() == null) {
            return NO_INFO;
        }
        // SQL AVG의 원시 소수(19.3333…)가 그대로 노출되지 않게 소수 1자리로 반올림
        String value = Math.round(fire.getDongFireAvgCnt() * 10.0) / 10.0 + "건";
        if (fire.getDongFireQuartile() != null) {
            value += "(" + quartileShortLabel(fire.getDongFireQuartile()) + ")";
        }
        return value;
    }

    private static String quartileShortLabel(int quartile) {
        if (quartile == 1) {
            return "서울 하위 25%";
        }
        if (quartile == 2) {
            return "서울 하위 25~50%";
        }
        if (quartile == 3) {
            return "서울 상위 25~50%";
        }
        return "서울 상위 25%";
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
