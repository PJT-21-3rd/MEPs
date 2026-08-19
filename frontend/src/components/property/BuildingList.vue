<template>
  <div class="px-6 pb-3">
    <div class="grid h-11 w-full grid-cols-3 rounded-xl bg-surface-gray p-1">
      <button
        v-for="tab in TABS"
        :key="tab.value"
        @click="activeTab = tab.value"
        class="rounded-lg text-[14px] transition-all duration-200 cursor-pointer"
        :class="
          activeTab === tab.value
            ? 'bg-white text-text-main shadow-sm'
            : 'text-text-modal hover:text-text-detail'
        "
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="mt-4 flex items-center justify-between">
      <p class="text-[14px] text-text-modal">
        총 <span class="text-text-main font-bold"> {{ displayBuildings.length }} </span>개
      </p>

      <div v-if="activeTab === 'nearby'" class="relative" ref="sortDropdownRef">
        <button
          type="button"
          @click="isSortOpen = !isSortOpen"
          class="flex h-9 w-[110px] rounded-lg border-0 bg-surface-gray items-center justify-between px-3 text-[14px] text-text-main outline-none"
        >
          <span>{{ currentSortLabel }}</span>
          <ChevronDown :size="16" class="text-text-sub" :class="{ 'rotate-180': isSortOpen }" />
        </button>

        <Transition name="fade-slide">
          <div
            v-if="isSortOpen"
            class="absolute right-0 top-11 z-50 w-[110px] overflow-hidden rounded-lg border border-surface-gray bg-white p-1 shadow-lg"
          >
            <div class="flex flex-col gap-1">
              <button
                v-for="option in SORT_OPTIONS"
                :key="option.value"
                @click="selectSort(option.value)"
                class="flex w-full items-center justify-between rounded-lg px-3 py-2 text-left text-[13px] transition-colors"
                :class="
                  uiStore.currentSort === option.value
                    ? 'bg-surface-gray text-text-main'
                    : 'text-text-secondary hover:bg-surface-base'
                "
              >
                <span>{{ option.label }}</span>
                <Check
                  v-if="uiStore.currentSort === option.value"
                  :size="14"
                  class="text-text-secondary"
                />
              </button>
            </div>
          </div>
        </Transition>
      </div>
      <p v-else-if="activeTab === 'recent'" class="text-[12px] text-text-sub">
        최근 본 20개까지 저장됩니다.
      </p>
    </div>
  </div>

  <div class="px-6 pb-6 pt-1">
    <template v-if="uiStore.isBuildingsLoading">
      <BuildingCardSkeleton />
    </template>

    <template v-else-if="displayBuildings.length > 0">
      <div class="flex flex-col gap-3">
        <BuildingCard
          v-for="building in displayBuildings"
          :key="building.buildingId"
          :building="building"
          @click="openDetail(building)"
        />
      </div>
    </template>

    <template v-else>
      <div class="flex flex-col items-center justify-center h-40 gap-2 text-center">
        <span class="text-[40px]">🏢</span>
        <p class="text-[14px] text-text-sub">
          {{ activeTab === 'recent' ? '최근 본 매물이 없습니다.' : '해당하는 매물이 없습니다.' }}
        </p>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useUiStore } from '@/stores/uiStore.js';
import { getSavedList } from '@/api/saved.js';
import { Check, ChevronDown } from '@lucide/vue';
import { useClickOutside } from '@/hooks/useClickOutside.js';
import BuildingCard from './BuildingCard.vue';
import BuildingCardSkeleton from './BuildingCardSkeleton.vue';

const router = useRouter();
const uiStore = useUiStore();

const TABS = [
  { label: '주변 상가', value: 'nearby' },
  { label: '최근 본', value: 'recent' },
  { label: '찜한 상가', value: 'scrapped' },
];
const activeTab = ref('nearby');

const SORT_OPTIONS = [
  { label: '랭킹순', value: 'POPULAR' },
  { label: '최신순', value: 'LATEST' },
  { label: '면적순', value: 'AREA' },
];

const isSortOpen = ref(false);
const sortDropdownRef = ref(null);
const savedBuildings = ref([]);

const currentSortLabel = computed(() => {
  const found = SORT_OPTIONS.find((o) => o.value === uiStore.currentSort);
  return found ? found.label : '랭킹순';
});

const selectSort = (val) => {
  uiStore.setSort(val);
  isSortOpen.value = false;
};

useClickOutside(sortDropdownRef, () => {
  isSortOpen.value = false;
});

// 찜한 리스트
watch(activeTab, async (newTab) => {
  if (newTab === 'scrapped') {
    uiStore.setBuildingsLoading(true);

    try {
      savedBuildings.value = await getSavedList();
    } catch (error) {
      console.error('찜한 목록을 불러오지 못했습니다.', error);
      savedBuildings.value = [];
    } finally {
      uiStore.setBuildingsLoading(false);
    }
  }
});

// 매물 리스트 분기
const displayBuildings = computed(() => {
  if (activeTab.value === 'recent') return uiStore.recentBuildings;
  if (activeTab.value === 'scrapped') return savedBuildings.value;
  return uiStore.currentBuildings || [];
});

const openDetail = (building) => {
  uiStore.openBuildingDetail(building.buildingId);
  if (router) {
    router.push({ query: { ...router.currentRoute.value.query, buildingId: building.buildingId } });
  }
};
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.15s ease-out;
}
.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
