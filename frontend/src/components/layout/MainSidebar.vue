<!-- src/components/layout/MainSidebar.vue -->
<template>
  <aside
    class="relative z-1000 flex h-full w-[400px] shrink-0 flex-col bg-white shadow-[1px_0_0_0_rgba(0,0,0,0.06)]"
  >
    <SidebarHeader />

    <template v-if="uiStore.isDetailOpen">
      <BuildingDetail />
    </template>

    <template v-else>
      <!-- 검색창 -->
      <div class="px-6 pb-4 pt-2">
        <template v-if="!uiStore.isDetailOpen">
          <SearchBar />
        </template>
      </div>

      <div class="relative flex-1 overflow-y-auto">
        <div ref="sentinelRef" class="h-px" />
        <template v-if="uiStore.currentBriefing">
          <!-- 스티키 헤더 -->
          <div class="sticky top-0 z-20 px-6">
            <div
              class="transition-all duration-200"
              :class="
                briefingStuck
                  ? 'rounded-xl border border-white/60 bg-surface-blue/70 px-4 py-3 shadow-[0_4px_14px_-6px_rgba(0,70,122,0.35)] backdrop-blur-md'
                  : 'rounded-t-2xl bg-surface-blue px-5 pb-3 pt-4'
              "
            >
              <div class="flex items-center gap-2">
                <span
                  v-if="!briefingStuck"
                  class="flex items-center gap-1 rounded-full bg-button-primary px-2.5 py-1 text-[12px] text-white"
                >
                  <Sparkles :size="12" /> AI 브리핑
                </span>
                <p class="text-[18px] tracking-tight text-text-main">
                  {{
                    uiStore.briefingLevel === 'gu'
                      ? uiStore.currentBriefing.sggName
                      : uiStore.currentBriefing.hjdName
                  }}
                </p>
                <p v-if="uiStore.briefingLevel === 'dong'" class="text-[14px] text-text-sub">
                  {{ uiStore.currentBriefing.sggName }}
                </p>
              </div>
            </div>
          </div>
          <CommercialAiBriefing :summary="uiStore.currentBriefing" />
        </template>

        <!-- 상권 요약 카드 -->
        <template v-else>
          <div class="sticky top-0 z-20 px-6">
            <div class="rounded-t-2xl bg-surface-blue px-5 pb-3 pt-4">
              <div class="flex items-center gap-2">
                <span
                  class="flex items-center gap-1 rounded-full bg-button-primary px-2.5 py-1 text-[12px] text-white"
                >
                  <Sparkles :size="12" /> AI 브리핑
                </span>
                <div class="h-6 w-32 rounded bg-surface-gray animate-pulse" />
              </div>
            </div>
          </div>
          <CommercialAiBriefingSkeleton />
        </template>

        <!-- 매물 리스트 -->
        <BuildingList />

        <div class="px-6">
          <FeedbackBanner />
        </div>

        <Footer />
      </div>
    </template>
  </aside>
</template>

<script setup>
import { ref, onUnmounted, watch } from 'vue';
import { Sparkles } from '@lucide/vue';
import { useUiStore } from '@/stores/uiStore.js';
import SearchBar from '../map/SearchBar.vue';
import SidebarHeader from './SidebarHeader.vue';
import CommercialAiBriefing from '../property/CommercialAiBriefing.vue';
import CommercialAiBriefingSkeleton from '../property/CommercialAiBriefingSkeleton.vue';
import BuildingList from '../property/BuildingList.vue';
import BuildingDetail from '../detail/BuildingDetail.vue';
import Footer from './Footer.vue';
import FeedbackBanner from '../common/FeedbackBanner.vue';

const uiStore = useUiStore();

const sentinelRef = ref(null);
const briefingStuck = ref(false);
let observer = null;

watch(sentinelRef, (newEl) => {
  if (newEl) {
    if (observer) observer.disconnect();

    observer = new IntersectionObserver(
      ([entry]) => {
        briefingStuck.value = !entry.isIntersecting;
      },
      { threshold: 0 },
    );
    observer.observe(newEl);
  }
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
});
</script>
