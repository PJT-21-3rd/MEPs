package org.meps.safetyreport.service;

import lombok.extern.slf4j.Slf4j;
import org.meps.safetyreport.dto.BasicBriefingDto;
import org.meps.safetyreport.dto.SafetyReportRowDto;
import org.meps.safetyreport.mapper.SafetyReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * MySQL 상태 컬럼(brief_status) 기반 코디네이터.
 *
 * 원자적 UPDATE의 affected rows가 리더를 판정하고(InnoDB 행 락), 탈락자는 커넥션을
 * 잡아두지 않고 폴링으로 결과를 기다린다 — GET_LOCK처럼 대기자가 커넥션 풀을
 * 소진시키는 구조를 피하기 위한 선택. 폴링 트래픽은 JVM single-flight가 앞단에서
 * 요청을 압축해 주므로 사용자 수가 아닌 인스턴스 수에 비례한다
 */
// @Primary: ReportGenerationCoordinator의 기본 구현체 선언
@Slf4j
@Primary
@Component
public class MysqlReportGenerationCoordinator implements ReportGenerationCoordinator {

    // 리더 경로 최악(LLM read timeout 5초 + 저장)보다 여유 있게 10초까지 기다린다
    static final int POLL_MAX_ATTEMPTS = 10;

    private final SafetyReportMapper safetyReportMapper;
    private final long pollIntervalMillis;

    @Autowired
    public MysqlReportGenerationCoordinator(SafetyReportMapper safetyReportMapper) {
        this(safetyReportMapper, 1000L);
    }

    MysqlReportGenerationCoordinator(SafetyReportMapper safetyReportMapper, long pollIntervalMillis) {
        this.safetyReportMapper = safetyReportMapper;
        this.pollIntervalMillis = pollIntervalMillis;
    }

    @Override
    public boolean tryClaim(String buildingId) {
        return safetyReportMapper.tryClaimBriefGeneration(buildingId) == 1;
    }

    @Override
    public void complete(String buildingId, String aiModelNm, BasicBriefingDto briefs) {
        safetyReportMapper.updateBriefs(buildingId, aiModelNm, briefs, "LLM");
    }

    @Override
    public void completeFallback(String buildingId, String aiModelNm, BasicBriefingDto briefs) {
        safetyReportMapper.updateBriefs(buildingId, aiModelNm, briefs, "FALLBACK");
    }

    /**
     * 선-조회 후-sleep — 다른 인스턴스가 이미 완료했으면 첫 조회에서 즉시 반환된다.
     * 리더 실패와 타이밍이 겹치는 드문 경우는 null 반환 후 호출자의 폴백으로 수용
     */
    @Override
    public BasicBriefingDto awaitResult(String buildingId) {
        for (int attempt = 1; attempt <= POLL_MAX_ATTEMPTS; attempt++) {
            SafetyReportRowDto row = safetyReportMapper.findByBdMgtSn(buildingId);
            if (row != null && row.hasAllBriefs()) {
                return row.toBriefingDto();
            }
            if (attempt == POLL_MAX_ATTEMPTS) {
                break;
            }
            try {
                Thread.sleep(pollIntervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        log.warn("브리핑 생성 대기 폴링 시간 초과. buildingId={}, attempts={}", buildingId, POLL_MAX_ATTEMPTS);
        return null;
    }
}
