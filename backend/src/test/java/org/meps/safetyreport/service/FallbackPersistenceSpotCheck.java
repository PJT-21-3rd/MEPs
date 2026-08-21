package org.meps.safetyreport.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.safetyreport.dto.BasicReportResponseDto;
import org.meps.safetyreport.dto.SafetyReportRowDto;
import org.meps.safetyreport.mapper.SafetyReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * brief_source/generated_at 기반 폴백 영속화가 실제 DB에서 의도대로 동작하는지 확인
 * 실행 전 해당 건물의 brief 컬럼을 NULL로 리셋해 "생성 필요" 상태를 만들어둔다.
 * 실행: RUN_SPOT_CHECK=true ./gradlew test --tests "*FallbackPersistenceSpotCheck" -i
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
@EnabledIfEnvironmentVariable(named = "RUN_SPOT_CHECK", matches = "true")
class FallbackPersistenceSpotCheck {

    private static final String BUILDING_ID = "1121510100101310011002352";

    @Autowired
    private BasicReportService basicReportService;
    @Autowired
    private SafetyReportMapper safetyReportMapper;

    @Test
    void firstCallPersistsResultThenSecondCallReusesWithoutRegenerating() {
        BasicReportResponseDto first = basicReportService.getBasicReport(BUILDING_ID, true);
        SafetyReportRowDto afterFirst = safetyReportMapper.findByBdMgtSn(BUILDING_ID);

        System.out.println("=== 1차 호출 후 ===");
        System.out.println("overallBriefing=" + first.getOverallBriefing());
        System.out.println("brief_source=" + afterFirst.getBriefSource());
        System.out.println("generated_at=" + afterFirst.getGeneratedAt());
        System.out.println("hasAllBriefs=" + afterFirst.hasAllBriefs());

        BasicReportResponseDto second = basicReportService.getBasicReport(BUILDING_ID, true);
        SafetyReportRowDto afterSecond = safetyReportMapper.findByBdMgtSn(BUILDING_ID);

        System.out.println("=== 2차 호출 후 (캐시 재사용 기대) ===");
        System.out.println("overallBriefing=" + second.getOverallBriefing());
        System.out.println("generated_at 변화 없음(같은 값)이어야 정상=" + afterSecond.getGeneratedAt());
        System.out.println("1차/2차 응답 동일=" + first.getOverallBriefing().equals(second.getOverallBriefing()));
    }
}
