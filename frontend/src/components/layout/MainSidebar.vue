<!-- src/components/layout/MainSidebar.vue -->
<template>
  <aside class="w-[400px] h-full">
    <SidebarHeader />

    <!-- 검색창 -->
    <div class="px-6 pb-4 pt-2">
      <template v-if="!uiStore.isDetailOpen">
        <SearchBar />
      </template>
      <!-- <template v-else>
        <div class="p-4 bg-white rounded-lg border border-primary shadow-sm">
          <h3 class="font-bold text-primary text-lg">건물 상세 정보</h3>
          <p class="text-text-sub mt-2">이곳에 건물 정보가 렌더링됩니다.</p>
          <button
            @click="uiStore.isDetailOpen = false"
            class="mt-4 px-4 py-2 bg-surface-gray rounded-md text-sm"
          >
            뒤로 가기
          </button>
        </div>
      </template> -->
    </div>

    <!-- 상권 요약 카드 -->
    <div class="px-6 pb-5 pt-1">
      <CommercialAiBriefing />
    </div>

    <!-- 매물 리스트 -->
    <div class="px-6 pb-3">
      <!-- <BuildingList /> -->
    </div>
  </aside>
</template>

<script setup>
import { useUiStore } from '@/stores/uiStore.js';
import SearchBar from '../map/SearchBar.vue';
import SidebarHeader from './SidebarHeader.vue';
import CommercialAiBriefing from '../property/CommercialAiBriefing.vue';
// import BuildingList from '../property/BuildingList.vue';

const uiStore = useUiStore();
</script>
