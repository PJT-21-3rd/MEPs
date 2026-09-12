package org.meps.safetyreport.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.meps.common.llm.LlmCallFailedException;
import org.meps.safetyreport.dto.BasicBriefingDto;
import org.meps.safetyreport.dto.BriefingInput;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** single-flight 리더 선출·결과 공유·클레임 위임 검증 */
class SingleFlightBriefGeneratorTest {

    private static final String BUILDING_ID = "1111111111111111111111111";

    private final BriefingInput input = BriefingInput.builder().build();

    @Test
    @DisplayName("클레임에 성공한 리더는 LLM으로 생성하고 complete로 저장한다")
    void leaderGeneratesAndCompletes() {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = true;
        StubBriefingService briefingService = new StubBriefingService();
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        BasicBriefingDto result = generator.generateOnce(BUILDING_ID, input);

        assertThat(result.getTotalBrief()).isEqualTo("생성된 브리핑");
        assertThat(briefingService.generateCallCount.get()).isEqualTo(1);
        assertThat(coordinator.completedBriefs).isSameAs(result);
        assertThat(coordinator.completedModelNm).isEqualTo("stub-model");
        assertThat(coordinator.completeFallbackCallCount).isZero();
    }

    @Test
    @DisplayName("LLM 실패 시 폴백을 FALLBACK 출처로 저장하고 반환한다")
    void llmFailureSavesFallbackAndReturnsIt() {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = true;
        StubBriefingService briefingService = new StubBriefingService();
        briefingService.failOnGenerate = true;
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        BasicBriefingDto result = generator.generateOnce(BUILDING_ID, input);

        assertThat(result.getTotalBrief()).isEqualTo("폴백 브리핑");
        assertThat(coordinator.completeFallbackCallCount).isEqualTo(1);
        assertThat(coordinator.fallbackCompletedBriefs).isSameAs(result);
        assertThat(coordinator.fallbackCompletedModelNm).isEqualTo("stub-model");
        assertThat(coordinator.completedBriefs).isNull();
    }

    @Test
    @DisplayName("클레임에 실패하면 생성 없이 다른 인스턴스의 결과를 기다려 반환한다")
    void claimLossReturnsAwaitedResult() {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = false;
        coordinator.awaitResultValue = BasicBriefingDto.builder().totalBrief("다른 인스턴스 결과").build();
        StubBriefingService briefingService = new StubBriefingService();
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        BasicBriefingDto result = generator.generateOnce(BUILDING_ID, input);

        assertThat(result.getTotalBrief()).isEqualTo("다른 인스턴스 결과");
        assertThat(briefingService.generateCallCount.get()).isZero();
    }

    @Test
    @DisplayName("클레임 실패 후 대기 결과도 없으면 폴백을 반환한다")
    void claimLossWithoutAwaitedResultFallsBack() {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = false;
        coordinator.awaitResultValue = null;
        StubBriefingService briefingService = new StubBriefingService();
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        BasicBriefingDto result = generator.generateOnce(BUILDING_ID, input);

        assertThat(result.getTotalBrief()).isEqualTo("폴백 브리핑");
        assertThat(briefingService.generateCallCount.get()).isZero();
    }

    @Test
    @DisplayName("같은 건물 동시 요청은 생성 1회로 합쳐지고 대기자는 리더 결과를 공유받는다")
    void concurrentRequestsShareSingleGeneration() throws Exception {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = true;
        StubBriefingService briefingService = new StubBriefingService();
        CountDownLatch generateEntered = new CountDownLatch(1);
        CountDownLatch releaseGenerate = new CountDownLatch(1);
        briefingService.generateEntered = generateEntered;
        briefingService.releaseGenerate = releaseGenerate;
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        AtomicReference<BasicBriefingDto> leaderResult = new AtomicReference<>();
        AtomicReference<BasicBriefingDto> waiterResult = new AtomicReference<>();
        Thread leader = new Thread(() -> leaderResult.set(generator.generateOnce(BUILDING_ID, input)));
        Thread waiter = new Thread(() -> waiterResult.set(generator.generateOnce(BUILDING_ID, input)));

        leader.start();
        assertThat(generateEntered.await(3, TimeUnit.SECONDS)).isTrue(); // 리더가 LLM 생성 구간에 진입
        waiter.start();
        waitUntilWaiting(waiter); // 대기자가 future.get에서 블록될 때까지
        releaseGenerate.countDown();
        leader.join(3000);
        waiter.join(3000);

        assertThat(briefingService.generateCallCount.get()).isEqualTo(1);
        assertThat(leaderResult.get()).isNotNull();
        assertThat(waiterResult.get()).isSameAs(leaderResult.get());
    }

    @Test
    @DisplayName("리더 완료 후에는 맵이 비워져 다음 요청이 새로 생성에 진입한다")
    void nextRequestAfterCompletionGeneratesAgain() {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = true;
        StubBriefingService briefingService = new StubBriefingService();
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        generator.generateOnce(BUILDING_ID, input);
        generator.generateOnce(BUILDING_ID, input);

        assertThat(briefingService.generateCallCount.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("백그라운드 재생성은 클레임에 성공하면 생성해서 complete로 저장한다")
    void regenerateInBackground_claimSucceeds_generatesAndCompletes() {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = true;
        StubBriefingService briefingService = new StubBriefingService();
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        generator.regenerateInBackground(BUILDING_ID, input);

        assertThat(briefingService.generateCallCount.get()).isEqualTo(1);
        assertThat(coordinator.completedBriefs.getTotalBrief()).isEqualTo("생성된 브리핑");
        assertThat(coordinator.completeFallbackCallCount).isZero();
    }

    @Test
    @DisplayName("백그라운드 재생성도 실패하면 폴백을 다시 저장한다")
    void regenerateInBackground_generateFails_savesFallbackAgain() {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = true;
        StubBriefingService briefingService = new StubBriefingService();
        briefingService.failOnGenerate = true;
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        generator.regenerateInBackground(BUILDING_ID, input);

        assertThat(coordinator.completeFallbackCallCount).isEqualTo(1);
        assertThat(coordinator.fallbackCompletedBriefs.getTotalBrief()).isEqualTo("폴백 브리핑");
        assertThat(coordinator.completedBriefs).isNull();
    }

    @Test
    @DisplayName("백그라운드 재생성은 클레임에 실패하면(쿨다운 미경과 등) 아무것도 안 하고 끝난다")
    void regenerateInBackground_claimFails_doesNothing() {
        FakeCoordinator coordinator = new FakeCoordinator();
        coordinator.claimResult = false;
        StubBriefingService briefingService = new StubBriefingService();
        SingleFlightBriefGenerator generator = new SingleFlightBriefGenerator(coordinator, briefingService);

        generator.regenerateInBackground(BUILDING_ID, input);

        assertThat(briefingService.generateCallCount.get()).isZero();
        assertThat(coordinator.completedBriefs).isNull();
        assertThat(coordinator.completeFallbackCallCount).isZero();
    }

    private static void waitUntilWaiting(Thread thread) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (thread.getState() != Thread.State.WAITING && thread.getState() != Thread.State.TIMED_WAITING) {
            if (System.currentTimeMillis() > deadline) {
                throw new AssertionError("대기자 스레드가 블록 상태에 도달하지 못했다: " + thread.getState());
            }
            Thread.sleep(10);
        }
    }

    /** ReportGenerationCoordinator 대역 — 호출 기록만 남긴다 */
    private static class FakeCoordinator implements ReportGenerationCoordinator {
        private boolean claimResult;
        private BasicBriefingDto awaitResultValue;
        private BasicBriefingDto completedBriefs;
        private String completedModelNm;
        private BasicBriefingDto fallbackCompletedBriefs;
        private String fallbackCompletedModelNm;
        private int completeFallbackCallCount;

        @Override
        public boolean tryClaim(String buildingId) {
            return claimResult;
        }

        @Override
        public void complete(String buildingId, String aiModelNm, BasicBriefingDto briefs) {
            completedModelNm = aiModelNm;
            completedBriefs = briefs;
        }

        @Override
        public void completeFallback(String buildingId, String aiModelNm, BasicBriefingDto briefs) {
            completeFallbackCallCount++;
            fallbackCompletedModelNm = aiModelNm;
            fallbackCompletedBriefs = briefs;
        }

        @Override
        public BasicBriefingDto awaitResult(String buildingId) {
            return awaitResultValue;
        }
    }

    /** BasicBriefingService 대역 — 동시성 테스트를 위해 생성 구간을 래치로 잡아둘 수 있다 */
    private static class StubBriefingService extends BasicBriefingService {
        private final AtomicInteger generateCallCount = new AtomicInteger();
        private boolean failOnGenerate;
        private CountDownLatch generateEntered;
        private CountDownLatch releaseGenerate;

        StubBriefingService() {
            super(null, null);
        }

        @Override
        public BasicBriefingDto generate(BriefingInput input) {
            generateCallCount.incrementAndGet();
            if (generateEntered != null) {
                generateEntered.countDown();
            }
            if (releaseGenerate != null) {
                try {
                    releaseGenerate.await(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            if (failOnGenerate) {
                throw new LlmCallFailedException("LLM 호출 실패(테스트용)");
            }
            return BasicBriefingDto.builder().totalBrief("생성된 브리핑").build();
        }

        @Override
        public BasicBriefingDto fallback(BriefingInput input) {
            return BasicBriefingDto.builder().totalBrief("폴백 브리핑").build();
        }

        @Override
        public String getModelName() {
            return "stub-model";
        }
    }
}
