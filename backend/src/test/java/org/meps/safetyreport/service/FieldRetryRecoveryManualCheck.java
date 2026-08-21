package org.meps.safetyreport.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.common.util.SafetyGrade;
import org.meps.config.RootConfig;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.service.FireScoreService;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.service.FloodScoreService;
import org.meps.safetyreport.dto.BasicBriefingDto;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 필드 단위 폴백(오늘 BasicBriefingService.isValidSentence 도입분)이 났을 때, 위반 사유를 피드백하지
 * 않고 같은 입력으로 그냥 한 번 더(전체 5필드 재호출) 시도하면 그 필드가 통과했을지를 가늠한다.
 * building_safety_report는 건드리지 않고(캐시엔 32건뿐이라 표본이 안 됨) buildings 마스터 테이블에서
 * 직접 표본을 뽑아 basicBriefingService.generate()/fallback()을 호출한다(DB write 없음).
 *
 * 폴백 여부 판정은 반환된 문장이 fallback()의 해당 필드 고정 템플릿과 문자열이 정확히 같은지로
 * 검사한다 — isValidSentence()가 실패 시 그 템플릿을 그대로 넣기 때문에 오탐 가능성은 사실상 없다
 * (LLM이 우연히 고정 템플릿과 토씨 하나까지 같은 문장을 낼 확률은 무시 가능).
 *
 * 표본 건물 수 + 폴백난 필드 수만큼 재시도 호출이 추가로 발생해 실제 OpenAI 비용이 든다. 실행하려면:
 *   RUN_FIELD_RETRY_CHECK=true ./gradlew test --tests "*FieldRetryRecoveryManualCheck" -i
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@EnabledIfEnvironmentVariable(named = "RUN_FIELD_RETRY_CHECK", matches = "true")
class FieldRetryRecoveryManualCheck {

    private static final int SAMPLE_SIZE = 200;

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
    void measureRetryRecoveryRate() throws SQLException {
        List<String> buildingIds = fetchBuildingIds(SAMPLE_SIZE);
        System.out.println("표본 건물 수: " + buildingIds.size());

        int fieldsChecked = 0;
        int fieldsFellBack = 0;
        int recoveredOnRetry = 0;
        Map<String, Integer> failuresByField = new LinkedHashMap<>();
        Map<String, Integer> recoveredByField = new LinkedHashMap<>();
        List<String> unexpectedErrors = new ArrayList<>();

        int processed = 0;
        for (String buildingId : buildingIds) {
            BriefingInput input;
            try {
                input = buildInput(buildingId);
            } catch (RuntimeException e) {
                unexpectedErrors.add(buildingId + ": " + e);
                continue;
            }

            BasicBriefingDto expectedFallback = basicBriefingService.fallback(input);
            BasicBriefingDto first = basicBriefingService.generate(input);

            for (FieldCheck field : fieldsFor(input)) {
                if (field.noFacts()) {
                    continue; // 정보 없음 필드는 LLM을 아예 안 거치므로 이 실험 대상이 아님
                }
                fieldsChecked++;
                String actual = fieldValue(first, field.name());
                String fallbackText = fieldValue(expectedFallback, field.name());
                if (!actual.equals(fallbackText)) {
                    continue; // 정상 통과
                }
                fieldsFellBack++;
                failuresByField.merge(field.name(), 1, Integer::sum);

                // 같은 건물·입력으로 피드백 없이 한 번 더(전체 재호출) — 그 필드만 통과 여부 확인
                BasicBriefingDto retry = basicBriefingService.generate(input);
                String retryValue = fieldValue(retry, field.name());
                if (!retryValue.equals(fallbackText)) {
                    recoveredOnRetry++;
                    recoveredByField.merge(field.name(), 1, Integer::sum);
                }
            }

            processed++;
            if (processed % 20 == 0) {
                System.out.println("진행 " + processed + "/" + buildingIds.size());
            }
        }

        System.out.println("==== 결과 ====");
        System.out.println("검사한 필드 수(정보없음 제외)=" + fieldsChecked
                + " 폴백=" + fieldsFellBack
                + " (" + (fieldsChecked == 0 ? 0 : Math.round(100.0 * fieldsFellBack / fieldsChecked)) + "%)");
        System.out.println("재시도(피드백 없이 1회 재호출)로 복구=" + recoveredOnRetry + "/" + fieldsFellBack
                + " (" + (fieldsFellBack == 0 ? 0 : Math.round(100.0 * recoveredOnRetry / fieldsFellBack)) + "%)");
        System.out.println("필드별 폴백 건수:");
        failuresByField.forEach((f, count) -> System.out.println("  - " + f + ": " + count
                + " (복구 " + recoveredByField.getOrDefault(f, 0) + ")"));
        if (!unexpectedErrors.isEmpty()) {
            System.out.println("건물 데이터 조회 자체가 실패해 제외된 건수: " + unexpectedErrors.size());
            unexpectedErrors.forEach(System.out::println);
        }
    }

    private record FieldCheck(String name, boolean noFacts) {
    }

    private List<FieldCheck> fieldsFor(BriefingInput input) {
        return List.of(
                new FieldCheck("overall", false),
                new FieldCheck("structure", BriefingInput.NO_FACTS.equals(input.getStructFacts())),
                new FieldCheck("fire", BriefingInput.NO_FACTS.equals(input.getFireFacts())),
                new FieldCheck("sinkhole", BriefingInput.NO_FACTS.equals(input.getSinkFacts())),
                new FieldCheck("flood", BriefingInput.NO_FACTS.equals(input.getFloodFacts())));
    }

    private String fieldValue(BasicBriefingDto dto, String field) {
        return switch (field) {
            case "overall" -> dto.getTotalBrief();
            case "structure" -> dto.getStructBrief();
            case "fire" -> dto.getFireBrief();
            case "sinkhole" -> dto.getSinkBrief();
            case "flood" -> dto.getFloodBrief();
            default -> throw new IllegalArgumentException(field);
        };
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

    private List<String> fetchBuildingIds(int limit) throws SQLException {
        List<String> ids = new ArrayList<>();
        String sql = "SELECT bd_mgt_sn FROM buildings ORDER BY bd_mgt_sn LIMIT ?";
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
