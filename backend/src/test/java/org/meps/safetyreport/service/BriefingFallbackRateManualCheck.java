package org.meps.safetyreport.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.common.util.SafetyGrade;
import org.meps.config.RootConfig;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.service.FireScoreService;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.service.FloodScoreService;
import org.meps.safetyreport.dto.BriefingInput;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.sinkhole.service.SinkholeScoreService;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;
import org.meps.structure.service.StructuralStabilityScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * BriefingToneValidator 등 검증 실패로 실제 폴백이 얼마나 자주 발생하는지 측정
 * building_safety_report에 저장된 브리핑/점수는 변경하지 않는다
 * 실제 건물 사실을 재구성해 basicBriefingService.generate()만 직접 호출하고(OpenAI 호출만 발생,
 * DB write 없음), 성공/폴백 비율과 실패 사유(등급-어조 불일치 vs 길이/포맷 등)를 집계한다.
 *
 * 건물 수만큼 실제 OpenAI 비용이 발생해 기본적으로는 건너뛴다. 실행하려면:
 *   RUN_FALLBACK_RATE_CHECK=true ./gradlew test --tests "*BriefingFallbackRateManualCheck" -i
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@EnabledIfEnvironmentVariable(named = "RUN_FALLBACK_RATE_CHECK", matches = "true")
class BriefingFallbackRateManualCheck {

    // 표본 크기 — 필요하면 늘려서 재실행 (전수는 building_safety_report 건수만큼)
    private static final int SAMPLE_SIZE = 100;

    @Autowired
    private DataSource dataSource;
    @Autowired
    private FireScoreService fireScoreService;
    @Autowired
    private SinkholeScoreService sinkholeScoreService;
    @Autowired
    private StructuralStabilityScoreService structuralStabilityScoreService;
    @Autowired
    private FloodScoreService floodScoreService;
    @Autowired
    private TotalScoreService totalScoreService;
    @Autowired
    private BasicBriefingService basicBriefingService;

    @Test
    void measureFallbackRate() throws SQLException {
        List<String> buildingIds = fetchBuildingIds(SAMPLE_SIZE);
        System.out.println("표본 건물 수: " + buildingIds.size());

        int success = 0;
        Map<String, Integer> failureReasons = new TreeMap<>();
        List<String> unexpectedErrors = new ArrayList<>();

        for (String buildingId : buildingIds) {
            try {
                BriefingInput input = buildInput(buildingId);
                basicBriefingService.generate(input);
                success++;
            } catch (LlmCallFailedException e) {
                failureReasons.merge(categorize(e.getMessage()), 1, Integer::sum);
            } catch (RuntimeException e) {
                // 점수 서비스 조회 실패 등 브리핑 검증과 무관한 오류는 집계에서 분리
                unexpectedErrors.add(buildingId + ": " + e);
            }
        }

        int attempted = buildingIds.size() - unexpectedErrors.size();
        int fallback = attempted - success;
        System.out.println("시도=" + attempted + " 성공=" + success + " 폴백=" + fallback
                + " (" + (attempted == 0 ? 0 : Math.round(100.0 * fallback / attempted)) + "%)");
        failureReasons.forEach((reason, count) -> System.out.println("  - " + reason + ": " + count));
        if (!unexpectedErrors.isEmpty()) {
            System.out.println("건물 데이터 조회 자체가 실패해 제외된 건수: " + unexpectedErrors.size());
            unexpectedErrors.forEach(System.out::println);
        }
    }

    private BriefingInput buildInput(String buildingId) {
        FireScoreResult fire = fireScoreService.getFireScore(buildingId);
        SinkholeScoreResult sink = sinkholeScoreService.getSinkholeScore(buildingId);
        StructuralStabilityScoreResultDto struct = structuralStabilityScoreService.getStructuralStabilityScore(buildingId);
        FloodScoreResultDto flood = floodScoreService.getFloodScore(buildingId);
        int totalScore = totalScoreService.calculateTotalScore(
                struct.getScore(), fire.getScore(), sink.getScore(), flood.getScore());

        return BriefingInput.builder()
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
    }

    /** 예외 메시지 접두어로 어느 검증기가 걸렀는지 분류 */
    private String categorize(String message) {
        if (message == null) {
            return "unknown";
        }
        if (message.contains("등급-어조 불일치")) {
            return "tone-mismatch(BriefingToneValidator)";
        }
        if (message.contains("길이 이탈")) {
            return "length";
        }
        if (message.contains("행정동 표기 누락")) {
            return "fire-dong-label";
        }
        if (message.contains("JSON 파싱")) {
            return "json-parse";
        }
        if (message.contains("브리핑 누락")) {
            return "missing-key";
        }
        return "other: " + message;
    }

    private List<String> fetchBuildingIds(int limit) throws SQLException {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT bd_mgt_sn FROM building_safety_report ORDER BY bd_mgt_sn LIMIT ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString(1));
                }
            }
        }
        return ids;
    }
}
