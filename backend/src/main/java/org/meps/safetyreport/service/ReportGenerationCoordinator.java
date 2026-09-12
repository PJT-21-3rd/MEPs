package org.meps.safetyreport.service;

import org.meps.safetyreport.dto.BasicBriefingDto;

/**
 * 인스턴스 간 브리핑 생성 리더 선출·완료·해제·대기 추상화.
 *
 * 인스턴스 안의 중복은 JVM single-flight(SingleFlightBriefGenerator)가 막고,
 * 인스턴스 사이의 중복은 이 코디네이터가 막는다. 현재 구현은 MySQL 상태 컬럼 클레임이며,
 * ElastiCache 도입이 결정되면 Redisson 구현체로 교체한다.
 */
public interface ReportGenerationCoordinator {

    /** 생성 권한 선점. true면 호출자가 리더 — 반드시 complete 또는 completeFallback으로 끝내야 한다 */
    boolean tryClaim(String buildingId);

    /** 리더 전용: LLM이 생성한 브리핑 저장 + 완료 전이 (구현체가 원자성 보장) */
    void complete(String buildingId, String aiModelNm, BasicBriefingDto briefs);

    /**
     * 리더 전용: LLM 생성 실패 시 등급별 폴백 템플릿을 저장하고 완료 전이.
     * release처럼 클레임만 반납하고 끝내지 않는 이유 — 폴백도 저장해야 다음 요청이 바로
     * 응답받고, 재시도는 tryClaim의 쿨다운 조건(FALLBACK + 1시간 경과)이 별도로 연다
     */
    void completeFallback(String buildingId, String aiModelNm, BasicBriefingDto briefs);

    /** 클레임 탈락자 전용: 리더의 결과를 기다렸다가 반환. 시간 내 미완성이면 null */
    BasicBriefingDto awaitResult(String buildingId);
}
