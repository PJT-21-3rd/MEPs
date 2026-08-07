package org.meps.report.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.common.util.SafetyGrade;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.service.FireScoreService;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.service.FloodScoreService;
import org.meps.report.dto.BasicReportResponseDto;
import org.meps.report.dto.BriefingInput;
import org.meps.report.dto.FactorBriefingDto;
import org.meps.report.dto.SafetyBriefingDto;
import org.meps.report.dto.SafetyReportRowDto;
import org.meps.report.mapper.SafetyReportMapper;
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
 * AI 안심 진단 기본 리포트 — 룰 엔진 점수 조합 + building_safety_report 캐시 + AI 브리핑.
 *
 * 캐시 판단: 점수는 배치 원천에서 결정론적으로 나오므로 "재산출 점수 == 저장 점수"가
 * 곧 입력 불변 판정. 불일치면 upsert가 브리핑을 함께 무효화해 재생성을 유도한다.
 * 폴백 브리핑은 DB에 저장하지 않는다 — brief NULL 유지가 다음 요청의 재시도 트리거
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SafetyReportService {

    private static final String NO_FACTS = BriefingInput.NO_FACTS;

    private final FireScoreService fireScoreService;
    private final SinkholeScoreService sinkholeScoreService;
    private final StructuralStabilityScoreService structuralStabilityScoreService;
    private final FloodScoreService floodScoreService;
    private final TotalScoreService totalScoreService;
    private final BriefingService briefingService;
    private final SafetyReportMapper safetyReportMapper;

    public BasicReportResponseDto getBasicReport(String buildingId) {
        FireScoreResult fire = fireScoreService.getFireScore(buildingId); // 미존재 건물이면 여기서 404
        SinkholeScoreResult sink = sinkholeScoreService.getSinkholeScore(buildingId);
        StructuralStabilityScoreResultDto struct = structuralStabilityScoreService.getStructuralStabilityScore(buildingId);
        FloodScoreResultDto flood = floodScoreService.getFloodScore(buildingId);

        int totalScore = totalScoreService.calculateTotalScore(
                struct.getScore(), fire.getScore(), sink.getScore(), flood.getScore());

        BriefingInput input = BriefingInput.builder()
                .totalGrade(SafetyGrade.fromScore(totalScore))
                .structGrade(struct.getGrade())
                .structFacts(buildStructFacts(struct))
                .fireGrade(fire.getGrade())
                .fireFacts(buildFireFacts(fire))
                .sinkGrade(sink.getGrade())
                .sinkFacts(buildSinkFacts(sink))
                .floodGrade(flood.getGrade())
                .floodFacts(buildFloodFacts(flood))
                .build();

        SafetyReportRowDto row = safetyReportMapper.findByBdMgtSn(buildingId);
        boolean scoreChanged = row == null
                || row.getTotalScore() != totalScore
                || row.getFloodScore() != flood.getScore()
                || row.getSinkScore() != sink.getScore()
                || row.getFireScore() != fire.getScore()
                || row.getStructScore() != struct.getScore();

        if (scoreChanged) {
            safetyReportMapper.upsertScores(buildingId, briefingService.getModelName(),
                    totalScore, flood.getScore(), sink.getScore(), fire.getScore(), struct.getScore());
        }

        if (!scoreChanged && hasAllBriefs(row)) {
            return buildResponse(totalScore, input, SafetyBriefingDto.builder()
                    .totalBrief(row.getTotalBrief())
                    .structBrief(row.getStructBrief())
                    .fireBrief(row.getFireBrief())
                    .sinkBrief(row.getSinkBrief())
                    .floodBrief(row.getFloodBrief())
                    .build());
        }

        SafetyBriefingDto briefs;
        long startMillis = System.currentTimeMillis();
        try {
            briefs = briefingService.generate(input);
            log.info("AI 브리핑 생성 완료. buildingId={}, model={}, 소요={}ms",
                    buildingId, briefingService.getModelName(), System.currentTimeMillis() - startMillis);
            safetyReportMapper.updateBriefs(buildingId, briefingService.getModelName(), briefs);
        } catch (LlmCallFailedException e) {
            log.warn("AI 브리핑 생성 실패(소요={}ms), 템플릿 폴백 응답. buildingId={}",
                    System.currentTimeMillis() - startMillis, buildingId, e);
            briefs = briefingService.fallback(input);
        }
        return buildResponse(totalScore, input, briefs);
    }

    private boolean hasAllBriefs(SafetyReportRowDto row) {
        return row.getTotalBrief() != null
                && row.getStructBrief() != null
                && row.getFireBrief() != null
                && row.getSinkBrief() != null
                && row.getFloodBrief() != null;
    }

    /** factors 순서 고정: 구조 → 화재 → 지반침하 → 침수 (명세) */
    private BasicReportResponseDto buildResponse(int totalScore, BriefingInput input, SafetyBriefingDto briefs) {
        List<FactorBriefingDto> factors = new ArrayList<>();
        factors.add(factor("STRUCTURE", input.getStructGrade(), briefs.getStructBrief()));
        factors.add(factor("FIRE", input.getFireGrade(), briefs.getFireBrief()));
        factors.add(factor("SINKHOLE", input.getSinkGrade(), briefs.getSinkBrief()));
        factors.add(factor("FLOOD", input.getFloodGrade(), briefs.getFloodBrief()));

        return BasicReportResponseDto.builder()
                .safetyScore(totalScore)
                .overallStatus(input.getTotalGrade().name())
                .overallBriefing(briefs.getTotalBrief())
                .factors(factors)
                .build();
    }

    private FactorBriefingDto factor(String code, SafetyGrade grade, String briefing) {
        return FactorBriefingDto.builder()
                .code(code)
                .status(grade.name())
                .briefing(briefing)
                .build();
    }

    // FireScoreService의 소방서 거리 구간과 세트 (골든타임 산출 근거는 그쪽 주석 참조)
    private static final double STATION_GOLDEN_INNER_M = 1200.0;
    private static final double STATION_GOLDEN_LIMIT_M = 1800.0;

    /**
     * 화재 팩터 사실 나열. NULL은 "정보 없음" (FireScoreResult 주석 컨벤션).
     * 거리·접면엔 룰 엔진 구간 기준의 맥락 주석(골든타임, 도로 폭)을 붙여 LLM이
     * "골든타임 내 출동 가능" 같은 해석 문장을 환각 없이 쓸 수 있게 한다.
     * 패키지 프라이빗 — 단위 테스트 대상
     */
    static String buildFireFacts(FireScoreResult fire) {
        StringBuilder sb = new StringBuilder();
        sb.append("주구조: ").append(orNoInfo(fire.getStrctCdNm()));
        sb.append(" / 도로접면: ").append(roadFactsLabel(fire.getRoadSideCodeNm()));
        sb.append(" / 최근접 소방서: ");
        if (fire.getNearestStationNm() == null || fire.getNearestStationDistanceM() == null) {
            sb.append(NO_FACTS);
        } else {
            sb.append(fire.getNearestStationNm())
                    .append(' ')
                    .append(Math.round(fire.getNearestStationDistanceM()))
                    .append("m(").append(stationContext(fire.getNearestStationDistanceM())).append(')');
        }
        sb.append(" / 행정동 최근 3년 평균 화재: ");
        if (fire.getDongFireAvgCnt() == null) {
            sb.append(NO_FACTS);
        } else {
            // SQL AVG의 원시 소수(19.3333…)가 문장에 노출되지 않게 소수 1자리로 반올림
            sb.append(Math.round(fire.getDongFireAvgCnt() * 10.0) / 10.0).append('건');
            if (fire.getDongFireQuartile() != null) {
                sb.append('(').append(quartileLabel(fire.getDongFireQuartile())).append(')');
            }
        }
        return sb.toString();
    }

    /** 지반침하 팩터 사실 나열. 사고 있으면 건수 + 최근 사고의 시기·거리. 패키지 프라이빗 — 단위 테스트 대상 */
    static String buildSinkFacts(SinkholeScoreResult sink) {
        if (sink.getIncidentCount() == 0) {
            return "반경 500m 내 지반침하 사고 이력: 없음";
        }
        // 매퍼가 사고일 내림차순 정렬로 반환하므로 첫 건이 최근 사고
        SinkholeIncidentDto latest = sink.getIncidents().get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("반경 500m 내 지반침하 사고 이력: ").append(sink.getIncidentCount()).append('건');
        sb.append(" / 최근 사고: ").append(formatSagoDate(latest.getSagoDate()))
                .append(", 거리 ").append(Math.round(latest.getDistanceM())).append('m');
        return sb.toString();
    }

    /** 구조 팩터 사실 나열. 근거는 StructuralStabilityScoreService가 이미 정리한 factors 리스트를 그대로 인용한다 */
    static String buildStructFacts(StructuralStabilityScoreResultDto struct) {
        StringBuilder sb = new StringBuilder();
        sb.append("사용승인일: ").append(structFactorDetail(struct, "USE_APR_DAY"));
        sb.append(" / 주구조: ").append(structFactorDetail(struct, "STRUCTURE_TYPE"));
        sb.append(" / 위반건축물 여부: ").append(violationLabel(structFactorDetail(struct, "VIOLATION")));
        sb.append(" / 지하층: ").append(structFactorDetail(struct, "UNDERGROUND_FLOOR"));
        return sb.toString();
    }

    private static String structFactorDetail(StructuralStabilityScoreResultDto struct, String factorCode) {
        return struct.getFactors().stream()
                .filter(f -> f.getFactor().equals(factorCode))
                .findFirst()
                .map(StructuralFactorDto::getDetail)
                .orElse(NO_FACTS);
    }

    private static String violationLabel(String raw) {
        if ("Y".equals(raw)) {
            return "있음";
        }
        if ("N".equals(raw)) {
            return "없음";
        }
        return raw; // "정보 없음" 등은 그대로 노출
    }

    /** 침수 팩터 사실 나열
     * 이력 있으면 건수 + 최근 이력의 연도·등급·원인 */
    static String buildFloodFacts(FloodScoreResultDto flood) {
        if (!flood.isFloodHistory()) {
            return "최근 침수 이력: 없음";
        }
        // 매퍼가 연도 내림차순 정렬로 반환하므로 첫 건이 최근 이력
        FloodIncidentDto latest = flood.getIncidents().get(0);
        StringBuilder sb = new StringBuilder();
        sb.append("최근 침수 이력: ").append(flood.getIncidentCount()).append('건');
        sb.append(" / 최근 침수: ").append(latest.getYear()).append("년(")
                .append(latest.getGrade()).append("등급, ").append(orNoInfo(latest.getCause())).append(')');
        return sb.toString();
    }

    /** "20230401" → "2023년 4월". 형식이 다르면 원문 그대로 */
    private static String formatSagoDate(String sagoDate) {
        if (sagoDate == null || sagoDate.length() < 6) {
            return orNoInfo(sagoDate);
        }
        String year = sagoDate.substring(0, 4);
        String month = sagoDate.substring(4, 6);
        if (month.charAt(0) == '0') {
            month = month.substring(1);
        }
            return year + "년 " + month + "월";
    }

    private static String stationContext(double distanceM) {
        if (distanceM <= STATION_GOLDEN_INNER_M) {
            return "골든타임 내";
        }
        if (distanceM <= STATION_GOLDEN_LIMIT_M) {
            return "골든타임 경계";
        }
        return "골든타임 초과 가능";
    }

    /** 토지특성 접면 명칭에 도로 폭 의미를 병기 (구간 정의는 FireScoreService 주석 참조) */
    private static String roadFactsLabel(String roadSideCodeNm) {
        if (roadSideCodeNm == null || roadSideCodeNm.isBlank() || roadSideCodeNm.equals("지정되지않음")) {
            return NO_FACTS;
        }
        if (roadSideCodeNm.equals("맹지")) {
            return roadSideCodeNm + "(도로에 접하지 않음)";
        }
        if (roadSideCodeNm.startsWith("세로")) {
            if (roadSideCodeNm.contains("(불)")) {
                return roadSideCodeNm + "(차량 통행 불가)";
            }
            return roadSideCodeNm + "(폭 8m 미만 도로 접함)";
        }
        if (roadSideCodeNm.startsWith("소로")) {
            return roadSideCodeNm + "(폭 8~12m 도로 접함)";
        }
        if (roadSideCodeNm.startsWith("중로")) {
            return roadSideCodeNm + "(폭 12~25m 도로 접함)";
        }
        if (roadSideCodeNm.startsWith("광대")) {
            return roadSideCodeNm + "(폭 25m 이상 도로 접함)";
        }
        return roadSideCodeNm;
    }

    /** 많다/적다 해석을 병기 — 사분위만 주면 LLM이 방향을 뒤집는 사례가 있었다 (상위 25%를 "많지 않다"로) */
    private static String quartileLabel(int quartile) {
        if (quartile == 1) {
            return "서울 행정동 중 하위 25%, 적은 편";
        }
        if (quartile == 2) {
            return "서울 행정동 중 하위 25~50%";
        }
        if (quartile == 3) {
            return "서울 행정동 중 상위 25~50%";
        }
        return "서울 행정동 중 상위 25%, 많은 편";
    }

    private static String orNoInfo(String value) {
        if (value == null || value.isBlank()) {
            return NO_FACTS;
        }
        return value;
    }
}
