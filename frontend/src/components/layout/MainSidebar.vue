<!-- src/components/layout/MainSidebar.vue -->
<template>
  <aside
    class="relative z-30 flex h-full w-[400px] shrink-0 flex-col bg-white shadow-[1px_0_0_0_rgba(0,0,0,0.06)]"
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
              <p class="text-[18px] tracking-tight text-text-main">{{ apiData.adstrdName }}</p>
              <p class="text-[14px] text-text-sub">{{ apiData.sggName }}</p>
            </div>
          </div>
        </div>
        <!-- 상권 요약 카드 -->
        <CommercialAiBriefing :summary="apiData" />

        <!-- 매물 리스트 -->
        <BuildingList />
      </div>
    </template>
  </aside>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { Sparkles } from '@lucide/vue';
import { useUiStore } from '@/stores/uiStore.js';
import SearchBar from '../map/SearchBar.vue';
import SidebarHeader from './SidebarHeader.vue';
import CommercialAiBriefing from '../property/CommercialAiBriefing.vue';
import BuildingList from '../property/BuildingList.vue';
import BuildingDetail from '../detail/BuildingDetail.vue';

const uiStore = useUiStore();

const apiData = ref({
  sggName: '강남구',
  adstrdName: '대치동',
  dailyFlpop: 125000, // 120000 이상 숫자
  flpopChangeRate: 10.24, // 증감률 (Float)
  topIndustryName: '일반음식점', // 주요업종
  topIndustryEtcCnt: 3, // 기타 업종 수
  avgBuildingAge: 11.2, // 노후도
  majorAgeGroup: '20대', // 주요 연령층
  majorAgeRatio: 58.02, // 비율
  overallBriefing:
    '일 평균 유동인구 3.8만 명의 활발한 상권으로, 음식점·카페 창업 수요가 특히 높아요. 주 소비층은 20~30대 직장인이 58%로, 트렌디한 업종이 유리해요. 평균 노후도 15년으로 건물 상태는 양호하지만, 최근 3년간 저지대 침수가 2건 있었으니 1층 매장은 주의가 필요해요.',
});

const sentinelRef = ref(null);
const briefingStuck = ref(false);
let observer = null;

onMounted(() => {
  observer = new IntersectionObserver(
    ([entry]) => {
      briefingStuck.value = !entry.isIntersecting;
    },
    { threshold: 1.0 },
  );

  if (sentinelRef.value) {
    observer.observe(sentinelRef.value);
  }
});

onUnmounted(() => {
  if (observer && sentinelRef.value) {
    observer.unobserve(sentinelRef.value);
  }
});
</script>
