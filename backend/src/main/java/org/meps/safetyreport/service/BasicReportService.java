package org.meps.safetyreport.service;

import lombok.RequiredArgsConstructor;
import org.meps.common.util.SafetyGrade;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.service.FireScoreService;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.service.FloodScoreService;
import org.meps.safetyreport.dto.BasicReportResponseDto;
import org.meps.safetyreport.dto.BriefingInput;
import org.meps.safetyreport.dto.BasicFactorDto;
import org.meps.safetyreport.dto.BasicBriefingDto;
import org.meps.safetyreport.dto.SafetyReportRowDto;
import org.meps.safetyreport.mapper.SafetyReportMapper;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.sinkhole.service.SinkholeScoreService;
import org.meps.structure.dto.StructuralFactorDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;
import org.meps.structure.service.StructuralStabilityScoreService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 안심 진단 기본 리포트 — 룰 엔진 점수 조합 + AI 브리핑.
 * 브리핑은 매번 생성하지 않고 building_safety_report에 저장해 두고 재사용한다.
 *
 * 재사용 판단: 점수는 배치 원천에서 결정론적으로 나오므로 "재산출 점수 == 저장 점수"가
 * 곧 입력 불변 판정. 불일치면 upsert가 브리핑을 함께 무효화해 재생성을 유도한다.
 * 폴백 브리핑도 brief_source="FALLBACK"으로 저장되어 다음 요청은 즉시 재사용한다 —
 * 매 요청마다 재시도하던 이전 방식은 실패하기 쉬운 건물에서 반복 비용을 유발했다.
 * 대신 FALLBACK_RETRY_COOLDOWN이 지나면 백그라운드로 재시도를 트리거한다(현재 요청은 안 막음).
 * 저장된 브리핑이 없을 때의 생성은 SingleFlightBriefGenerator가 감싸 중복 LLM 호출을 막는다
 * (인스턴스 안은 JVM single-flight, 인스턴스 간은 brief_status 컬럼 클레임)
 */
@Service
@RequiredArgsConstructor
public class BasicReportService {

    private static final String NO_FACTS = BriefingInput.NO_FACTS;

    private static final Duration FALLBACK_RETRY_COOLDOWN = Duration.ofHours(1);

    private final FireScoreService fireScoreService;
    private final SinkholeScoreService sinkholeScoreService;
    private final StructuralStabilityScoreService structuralStabilityScoreService;
    private final FloodScoreService floodScoreService;
    private final TotalScoreService totalScoreService;
    private final BasicBriefingService basicBriefingService;
    private final SingleFlightBriefGenerator singleFlightBriefGenerator;
    private final SafetyReportMapper safetyReportMapper;
    private final DetailedReportService detailedReportService;

    /**
     * @param loggedIn 비로그인이면 factors 미노출(응답에서 필드 제거) — 프론트는 그 자리를
     *                 blur 플레이스홀더 + 로그인 유도로 렌더. 점수·브리핑 생성/저장은 동일하게 수행
     */
    public BasicReportResponseDto getBasicReport(String buildingId, boolean loggedIn) {
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
            safetyReportMapper.upsertScores(buildingId, basicBriefingService.getModelName(),
                    totalScore, flood.getScore(), sink.getScore(), fire.getScore(), struct.getScore());
            // 점수 변경(최초 조회 포함) 시 비동기로 상세 리포트 재생성.
            // 같은 건물이 이미 생성 중이면 requestDetailedReportAsync가 무시한다
            detailedReportService.requestDetailedReportAsync(buildingId);
        }

        if (!scoreChanged && row.hasAllBriefs()) {
            if (isFallbackCooldownExpired(row)) {
                // 실제 재클레임 가능 여부(쿨다운)는 tryClaimBriefGeneration이 DB에서 다시 판정하므로
                // 여기서 만료로 보여도 동시 요청끼리 중복 생성으로 이어지지 않는다
                singleFlightBriefGenerator.regenerateInBackground(buildingId, input);
            }
            return buildResponse(totalScore, loggedIn, input, row.toBriefingDto());
        }

        BasicBriefingDto briefs = singleFlightBriefGenerator.generateOnce(buildingId, input);
        return buildResponse(totalScore, loggedIn, input, briefs);
    }

    static boolean isFallbackCooldownExpired(SafetyReportRowDto row) {
        return row.isFallback()
                && row.getGeneratedAt() != null
                && row.getGeneratedAt().isBefore(LocalDateTime.now().minus(FALLBACK_RETRY_COOLDOWN));
    }

    /** factors 순서 고정: 구조 → 화재 → 지반침하 → 침수 */
    private BasicReportResponseDto buildResponse(int totalScore, boolean loggedIn, BriefingInput input, BasicBriefingDto briefs) {
        // 비로그인은 null 유지 — @JsonInclude(NON_NULL)로 응답에서 factors 필드 자체가 제거된다
        // (빈 리스트를 넣으면 []로 직렬화돼 프론트가 로그인 응답으로 오인)
        List<BasicFactorDto> factors = null;
        if (loggedIn) {
            factors = new ArrayList<>();
            factors.add(factor("STRUCTURE", input.getStructGrade(), briefs.getStructBrief()));
            factors.add(factor("FIRE", input.getFireGrade(), briefs.getFireBrief()));
            factors.add(factor("SINKHOLE", input.getSinkGrade(), briefs.getSinkBrief()));
            factors.add(factor("FLOOD", input.getFloodGrade(), briefs.getFloodBrief()));
        }

        return BasicReportResponseDto.builder()
                .safetyScore(totalScore)
                .overallStatus(input.getTotalGrade().name())
                .overallBriefing(briefs.getTotalBrief())
                .factors(factors)
                .build();
    }

    private BasicFactorDto factor(String code, SafetyGrade grade, String briefing) {
        return BasicFactorDto.builder()
                .code(code)
                .status(grade.name())
                .briefing(briefing)
                .build();
    }

    // FireScoreService의 소방서 거리 구간과 세트 (골든타임 산출 근거는 그쪽 주석 참조)
    private static final double STATION_GOLDEN_INNER_M = 1200.0;
    private static final double STATION_GOLDEN_LIMIT_M = 1800.0;

    /**
     * 화재 팩터 사실 나열. NULL은 "정보 없음"
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

    /** 지반침하 팩터 사실 나열 - 사고 있으면 건수 + 최근 사고의 시기·거리 */
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

    /** 구조 팩터 사실 나열 */
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

    /** 침수 팩터 사실 나열 - 이력 있으면 건수 + 최근 이력의 연도·등급·원인 */
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

    /** "20230401" → "2023년 4월". 형식이 다르면 원문 그대로. 상세 리포트 details 조립에서도 사용 */
    static String formatSagoDate(String sagoDate) {
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
