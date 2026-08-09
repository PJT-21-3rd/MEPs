<template>
  <div class="w-full h-full flex overflow-hidden relative">
    <MainSidebar />
    <div
      v-if="uiStore.isReportOpen"
      class="absolute top-0 bottom-0 left-[400px] z-20 w-[400px] bg-white shadow-2xl border-l border-surface-base flex flex-col"
    >
      <AiReportPanelWithModals :building-id="uiStore.selectedBuildingId" />
    </div>
    <!-- uiStore.selectedBuildingId : api 구현될 시 -->
    <!-- '1121510700102020000025797': 실제 buildingId 예시 -->

    <main class="relative h-full flex-1 overflow-hidden">
      <Transition
        enter-active-class="transition-opacity duration-300 ease-out"
        enter-from-class="opacity-0"
        enter-to-class="opacity-100"
        leave-active-class="transition-opacity duration-200 ease-in"
        leave-from-class="opacity-100"
        leave-to-class="opacity-0"
      >
        <ScreenRoadView v-if="uiStore.isRoadViewModalOpen" />
      </Transition>

      <NaverMap />

      <div v-if="uiStore.isDetailOpen" class="absolute top-5 left-6 z-20 w-[360px]">
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
import ScreenRoadView from '@/components/map/ScreenRoadView.vue';
import SearchBar from '@/components/map/SearchBar.vue';
import AiReportPanelWithModals from '@/components/report/AiReportPanelWithModals.vue';
import { useUiStore } from '@/stores/uiStore';

const uiStore = useUiStore();
</script>
