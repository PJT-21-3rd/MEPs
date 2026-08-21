package org.meps.safetyreport.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.safetyreport.dto.BasicBriefingDto;
import org.meps.safetyreport.dto.BriefingInput;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 브리핑 생성 single-flight — 같은 건물의 동시 생성 요청을 대표 1개로 합친다.
 *
 * 계층 1(이 클래스, JVM 안): putIfAbsent에 성공한 스레드만 생성 경로로 진행하고,
 * 나머지는 같은 Future의 결과에 편승한다. 덕분에 계층 2(코디네이터, 인스턴스 간)에
 * 도달하는 트래픽이 사용자 수가 아닌 인스턴스 수에 비례하게 된다
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SingleFlightBriefGenerator {

    // 대기 상한: 리더 경로 최악(LLM read timeout 5초 + 코디네이터 폴링 10초)보다 여유 있게
    private static final long WAITER_TIMEOUT_SECONDS = 15;

    private final ConcurrentHashMap<String, CompletableFuture<BasicBriefingDto>> inFlight
            = new ConcurrentHashMap<>();

    private final ReportGenerationCoordinator coordinator;
    private final BasicBriefingService basicBriefingService;

    /**
     * 저장된 브리핑이 없을 때의 생성 경로 전체를 감싼다. 폴백도 등급-source 표시("FALLBACK")와 함께
     * 생성 재시도는 BasicReportService가 쿨다운 경과를 보고 regenerateInBackground로 별도로 트리거한다
     */
    public BasicBriefingDto generateOnce(String buildingId, BriefingInput input) {
        // 맵 등록은 빈 Future만 — 등록 함수 안에서 LLM을 호출하면 해시 버킷 락을 장기 점유한다
        CompletableFuture<BasicBriefingDto> myFuture = new CompletableFuture<>();
        CompletableFuture<BasicBriefingDto> existing = inFlight.putIfAbsent(buildingId, myFuture);
        if (existing != null) {
            return awaitLeader(buildingId, existing, input);
        }
        try {
            BasicBriefingDto briefs = generateAsLeader(buildingId, input);
            myFuture.complete(briefs);
            return briefs;
        } catch (RuntimeException e) {
            // 예기치 못한 예외도 대기자에게 즉시 전파 — 대기자가 타임아웃까지 갇히지 않게
            myFuture.completeExceptionally(e);
            throw e;
        } finally {
            // 제거를 빼먹으면 성공 시 메모리 누수, 실패 시 그 건물이 영구 재시도 불가가 된다
            inFlight.remove(buildingId);
        }
    }

    private BasicBriefingDto generateAsLeader(String buildingId, BriefingInput input) {
        if (!coordinator.tryClaim(buildingId)) {
            // 다른 인스턴스가 생성 중이거나 방금 완료 — 결과를 기다렸다가 공유
            BasicBriefingDto briefs = coordinator.awaitResult(buildingId);
            if (briefs != null) {
                return briefs;
            }
            return basicBriefingService.fallback(input);
        }

        long startMillis = System.currentTimeMillis();
        try {
            BasicBriefingDto briefs = basicBriefingService.generate(input);
            log.info("AI 브리핑 생성 완료. buildingId={}, model={}, 소요={}ms",
                    buildingId, basicBriefingService.getModelName(), System.currentTimeMillis() - startMillis);
            coordinator.complete(buildingId, basicBriefingService.getModelName(), briefs);
            return briefs;
        } catch (LlmCallFailedException e) {
            log.warn("AI 브리핑 생성 실패(소요={}ms), 템플릿 폴백 응답. buildingId={}",
                    System.currentTimeMillis() - startMillis, buildingId, e);
            BasicBriefingDto fallback = basicBriefingService.fallback(input);
            coordinator.completeFallback(buildingId, basicBriefingService.getModelName(), fallback);
            return fallback;
        }
    }

    /** 대기자: 리더의 결과에 편승. 타임아웃·인터럽트·리더 이상 종료 시 폴백(저장은 리더만 한다) */
    private BasicBriefingDto awaitLeader(String buildingId,
                                         CompletableFuture<BasicBriefingDto> future, BriefingInput input) {
        try {
            return future.get(WAITER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return basicBriefingService.fallback(input);
        } catch (TimeoutException e) {
            log.warn("브리핑 리더 대기 시간 초과, 템플릿 폴백 응답. buildingId={}", buildingId);
            return basicBriefingService.fallback(input);
        } catch (Exception e) {
            log.warn("브리핑 리더 이상 종료, 템플릿 폴백 응답. buildingId={}", buildingId, e);
            return basicBriefingService.fallback(input);
        }
    }

    /**
     * 폴백으로 채워진 건물의 백그라운드 재시도
     * JVM 안 중복 트리거는 신경 쓰지 않는다: coordinator.tryClaim의 원자적 UPDATE가 동시에
     * 여러 번 걸려도 단 하나만 리더가 되고 나머지는 즉시 반환되어, 중복 OpenAI 호출로 이어지지 않는다
     */
    @Async("detailedReportExecutor")
    public void regenerateInBackground(String buildingId, BriefingInput input) {
        if (!coordinator.tryClaim(buildingId)) {
            return;
        }
        long startMillis = System.currentTimeMillis();
        try {
            BasicBriefingDto briefs = basicBriefingService.generate(input);
            log.info("AI 브리핑 백그라운드 재생성 성공. buildingId={}, model={}, 소요={}ms",
                    buildingId, basicBriefingService.getModelName(), System.currentTimeMillis() - startMillis);
            coordinator.complete(buildingId, basicBriefingService.getModelName(), briefs);
        } catch (LlmCallFailedException e) {
            log.warn("AI 브리핑 백그라운드 재생성도 실패(소요={}ms), 폴백 유지. buildingId={}",
                    System.currentTimeMillis() - startMillis, buildingId, e);
            coordinator.completeFallback(buildingId, basicBriefingService.getModelName(),
                    basicBriefingService.fallback(input));
        }
    }
}
