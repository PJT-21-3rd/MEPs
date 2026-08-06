<template>
  <div class="w-full h-full flex overflow-hidden relative">
    <MainSidebar class="" />
    <div
      v-if="uiStore.isReportOpen"
      class="absolute top-0 bottom-0 left-[400px] z-20 w-[400px] bg-white shadow-2xl border-l border-surface-base flex flex-col"
    >
      <AiReportPanel
        :building-id="4"
        building-name="동양빌딩"
        :initial-report-data="{
          score: 42,
          grade: 'danger',
          overallBriefing:
            '해당 건물은 구조·화재·침수 항목에서 위험 등급이 확인되어 정밀 점검이 필요합니다.',
          dangerItems: {
            structure: {
              status: 'danger',
              summary: '노후로 인한 구조적 위험이 확인되었습니다.',
            },
            fire: {
              status: 'danger',
              summary: '반복적인 화재 이력이 확인되었습니다.',
            },
            sinkhole: {
              status: 'warning',
              summary: '인근 지역 지반침하 사례가 보고되었습니다.',
            },
            flood: {
              status: 'danger',
              summary: '상습 침수 구역으로 반복적인 침수 이력이 있습니다.',
            },
          },
          disasterLiability: {
            required: true,
            description: '1층 음식점 의무 가입 · 미가입 시 과태료 최대 300만 원',
            evidenceTags: ['지상 1층', '업종 음식점'],
          },
          fireLiability: {
            required: true,
            description: '다중이용업소 해당 · 의무 가입 대상',
            evidenceTags: ['다중이용업소'],
          },
        }"
      />
    </div>

    <main class="relative h-full flex-1 overflow-hidden">
      <NaverMap />

      <div v-if="uiStore.isDetailOpen" class="absolute top-5 left-6 z-10 w-[360px]">
        <SearchBar />
      </div>
      <div v-else><QuickNavigation /></div>

      <MapResearch />
      <MapControls />
    </main>
  </div>
</template>

<script setup>
import MainSidebar from '@/components/layout/MainSidebar.vue';
import MapControls from '@/components/map/MapControls.vue';
import MapResearch from '@/components/map/MapResearch.vue';
import NaverMap from '@/components/map/NaverMap.vue';
import QuickNavigation from '@/components/map/QuickNavigation.vue';
import SearchBar from '@/components/map/SearchBar.vue';
import AiReportPanel from '@/components/report/AiReportPanel.vue';
import { useUiStore } from '@/stores/uiStore';

const uiStore = useUiStore();
</script>
