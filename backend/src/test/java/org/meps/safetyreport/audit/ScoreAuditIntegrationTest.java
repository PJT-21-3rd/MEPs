package org.meps.safetyreport.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.meps.config.RootConfig;
import org.meps.safetyreport.audit.service.ScoreAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 안전점수 전수조사 실행기 — 실 DB(3307 터널) 필요.
 * 일반 검증 테스트가 아니라 safety_score_audit 테이블을 다시 채우는 배치 트리거이므로
 * 점수 로직을 바꾼 뒤 분포를 다시 보고 싶을 때 단독 실행한다:
 * gradle test --tests org.meps.safetyreport.audit.ScoreAuditIntegrationTest
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RootConfig.class)
class ScoreAuditIntegrationTest {

    @Autowired
    private ScoreAuditService scoreAuditService;

    @Test
    @DisplayName("전 건물 안전점수를 전수 산출해 safety_score_audit 테이블에 적재한다")
    void runFullAudit() {
        int count = scoreAuditService.runFullAudit();
        assertThat(count).isGreaterThan(300_000);
    }
}
