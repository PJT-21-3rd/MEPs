package org.meps.safetyreport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.common.util.SafetyGrade;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.service.FireScoreService;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.service.FloodScoreService;
import org.meps.safetyreport.dto.*;
import org.meps.safetyreport.mapper.SafetyReportMapper;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.sinkhole.service.SinkholeScoreService;
import org.meps.structure.dto.StructuralFactorDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;
import org.meps.structure.service.StructuralStabilityScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;

/**
 * AI 안심 진단 상세 리포트
 *
 * details는 각 점수 서비스가 판정에 실제 사용한 값만 출처와 함께 노출
 * aiReport는 LLM 3단락 해석(근거→리스크→솔루션)으로 채움
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DetailedReportService {

    private static final String NO_INFO = BriefingInput.NO_FACTS;

    private final FireScoreService fireScoreService;
    private final SinkholeScoreService sinkholeScoreService;
    private final StructuralStabilityScoreService structuralStabilityScoreService;
    private final FloodScoreService floodScoreService;
    private final TotalScoreService totalScoreService;
    private final DetailedBriefingService detailedBriefingService;
    private final SafetyReportMapper safetyReportMapper;

    /**
     * 백그라운드 생성이 시작(대기열 등록)~완료 사이인 건물들.
     * 같은 건물의 작업이 스레드 풀 대기열에 두 번 들어가지 않게 앞단에서 걸러낸다
     */
    final Set<String> inFlightBuildings = ConcurrentHashMap.newKeySet();

    /** 같은 빈 안에서 @Async 메서드를 직접 호출하면 프록시를 타지 않아 동기 실행되므로, 자기 프록시를 주입받아 호출한다 */
    @Autowired @Lazy
    DetailedReportService self;

    /**
     * 점수는 DB와 비교하지 않는다 — 그 판정은 기본 리포트 조회 시점(BasicReportService)에서만
     * 이루어지고, 점수가 바뀌었을 때만 여기까지 (비동기로) 트리거된다. 여기서는 저장 행에
     * report 5개 컬럼이 이미 채워져 있는지만 보고, 없으면 생성해서 채운다
     */
    public DetailedReportResponseDto getDetailedReport(String buildingId) {
        FireScoreResult fire = fireScoreService.getFireScore(buildingId); // 미존재 건물이면 여기서 404
        SinkholeScoreResult sink = sinkholeScoreService.getSinkholeScore(buildingId);
        StructuralStabilityScoreResultDto struct = structuralStabilityScoreService.getStructuralStabilityScore(buildingId);
        FloodScoreResultDto flood = floodScoreService.getFloodScore(buildingId);

        SafetyReportRowDto row = safetyReportMapper.findByBdMgtSn(buildingId);

        String totalReport;
        String structReport;
        String fireReport;
        String sinkReport;
        String floodReport;

        if (row != null && hasAllReports(row)) {
            totalReport = row.getTotalReport();
            structReport = row.getStructReport();
            fireReport = row.getFireReport();
            sinkReport = row.getSinkReport();
            floodReport = row.getFloodReport();
        } else {
            int totalScore = totalScoreService.calculateTotalScore(
                    struct.getScore(), fire.getScore(), sink.getScore(), flood.getScore());

            BriefingInput input = BriefingInput.builder()
                    .totalGrade(SafetyGrade.fromScore(totalScore))
                    .structGrade(struct.getGrade())
                    .structFacts(BasicReportService.buildStructFacts(struct))
                    .fireGrade(fire.getGrade())
                    .fireFacts(BasicReportService.buildFireFacts(fire))
                    .sinkGrade(sink.getGrade())
                    .sinkFacts(BasicReportService.buildSinkFacts(sink))
                    .floodGrade(flood.getGrade())
                    .floodFacts(BasicReportService.buildFloodFacts(flood))
                    .build();

            DetailedBriefingDto briefing;
            long startMillis = System.currentTimeMillis();
            try {
                briefing = detailedBriefingService.generate(input);
                log.info("AI 상세 리포트 생성 완료. buildingId={}, 소요={}ms",
                        buildingId, System.currentTimeMillis() - startMillis);
                totalReport = briefing.getTotalHeadline() + "\n\n" + briefing.getTotalSummary();
                safetyReportMapper.updateReports(buildingId, totalReport,
                        briefing.getFloodReport(), briefing.getSinkReport(),
                        briefing.getFireReport(), briefing.getStructReport());
            } catch (LlmCallFailedException e) {
                log.warn("AI 상세 리포트 생성 실패(소요={}ms), 템플릿 폴백 응답. buildingId={}",
                        System.currentTimeMillis() - startMillis, buildingId, e);
                briefing = detailedBriefingService.fallback(input);
                totalReport = briefing.getTotalHeadline() + "\n\n" + briefing.getTotalSummary();
            }
            structReport = briefing.getStructReport();
            fireReport = briefing.getFireReport();
            sinkReport = briefing.getSinkReport();
            floodReport = briefing.getFloodReport();
        }

        // factors 순서 고정: 구조 → 화재 → 지반침하 → 침수 (명세)
        List<DetailedFactorDto> factors = new ArrayList<>();
        factors.add(buildFactor("STRUCTURE", struct.getGrade(), structReport, buildStructDetails(struct)));
        factors.add(buildFactor("FIRE", fire.getGrade(), fireReport, buildFireDetails(fire)));
        factors.add(buildFactor("SINKHOLE", sink.getGrade(), sinkReport, buildSinkDetails(sink)));
        factors.add(buildFactor("FLOOD", flood.getGrade(), floodReport, buildFloodDetails(flood)));

        return DetailedReportResponseDto.builder()
                .overallAiReport(totalReport)
                .factors(factors)
                .build();
    }

    /**
     * BasicReportService가 점수 변경을 감지했을 때 호출하는 진입점.
     * 같은 건물이 이미 생성 대기/진행 중이면 아무것도 하지 않는다 — 백그라운드 작업이라
     * 결과를 기다리는 쪽이 없으므로, 중복이면 조용히 빠지는 것으로 충분하다
     */
    public void requestDetailedReportAsync(String buildingId) {
        if (!inFlightBuildings.add(buildingId)) {
            return;
        }
        try {
            self.generateDetailedReportAsync(buildingId);
        } catch (RejectedExecutionException e) {
            // 스레드 풀 거부(큐 포화)로 기본 리포트 응답까지 실패시키지 않는다 —
            // 상세 리포트는 상세 조회 시점의 동기 경로가 다시 생성하므로 버리고 로그만 남긴다
            inFlightBuildings.remove(buildingId);
            log.warn("상세 리포트 백그라운드 생성이 거부되어 건너뜀(큐 포화). buildingId={}", buildingId, e);
        } catch (RuntimeException e) {
            // 거부 외의 실패에서도 표식을 남겨두면 그 건물이 영구 재시도 불가가 된다
            inFlightBuildings.remove(buildingId);
            throw e;
        }
    }

    @Async("detailedReportExecutor")
    public void generateDetailedReportAsync(String buildingId) {
        try {
            getDetailedReport(buildingId);
        } finally {
            inFlightBuildings.remove(buildingId);
        }
    }

    private boolean hasAllReports(SafetyReportRowDto row) {
        return row.getTotalReport() != null
                && row.getStructReport() != null
                && row.getFireReport() != null
                && row.getSinkReport() != null
                && row.getFloodReport() != null;
    }

    private DetailedFactorDto buildFactor(String code, SafetyGrade grade, String aiReport, List<DetailedFactorBaseDto> details) {
        return DetailedFactorDto.builder()
                .code(code)
                .status(grade.name())
                .aiReport(aiReport)
                .details(details)
                .build();
    }

    /** 구조 details */
    static List<DetailedFactorBaseDto> buildStructDetails(StructuralStabilityScoreResultDto struct) {
        List<DetailedFactorBaseDto> details = new ArrayList<>();
        details.add(buildDetail("주구조", findFactorDetail(struct, "STRUCTURE_TYPE"), "건축물대장 표제부"));
        details.add(buildDetail("사용승인일", findFactorDetail(struct, "USE_APR_DAY"), "건축물대장 표제부"));
        details.add(buildDetail("위반건축물 여부", violationValue(findFactorDetail(struct, "VIOLATION")), "건축물대장 표제부"));
        details.add(buildDetail("지하층수", findFactorDetail(struct, "UNDERGROUND_FLOOR"), "건축물대장 표제부"));
        return details;
    }

    /** 화재 details */
    static List<DetailedFactorBaseDto> buildFireDetails(FireScoreResult fire) {
        List<DetailedFactorBaseDto> details = new ArrayList<>();
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
    static List<DetailedFactorBaseDto> buildSinkDetails(SinkholeScoreResult sink) {
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

        List<DetailedFactorBaseDto> details = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            String value;
            if (counts[i] == 0) {
                value = "없음";
            } else {
                value = counts[i] + "건(최근 " + BasicReportService.formatSagoDate(latestSagoDates[i]) + ")";
            }
            details.add(buildDetail(labels[i], value, "지반침하 사고이력"));
        }
        return details;
    }

    /** 침수 details */
    static List<DetailedFactorBaseDto> buildFloodDetails(FloodScoreResultDto flood) {
        List<DetailedFactorBaseDto> details = new ArrayList<>();
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

    private static DetailedFactorBaseDto buildDetail(String label, String value, String source) {
        return DetailedFactorBaseDto.builder().label(label).value(value).source(source).build();
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
