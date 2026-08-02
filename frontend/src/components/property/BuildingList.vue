<template>
  <div class="px-6 pb-3">
    <div class="grid h-11 w-full grid-cols-3 rounded-xl bg-surface-gray p-1">
      <button
        v-for="tab in TABS"
        :key="tab.value"
        @click="activeTab = tab.value"
        class="rounded-lg text-[14px] transition-all duration-200"
        :class="
          activeTab === tab.value
            ? 'bg-white text-text-main shadow-sm'
            : 'text-neutral-500 hover:text-neutral-700'
        "
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="mt-4 flex items-center justify-between">
      <p class="text-[14px] text-neutral-500 font-medium">
        총 <span class="text-text-main font-bold"> {{ sortedBuildings.length }} </span>개
      </p>

      <div class="relative" ref="sortDropdownRef">
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
                  currentSort === option.value
                    ? 'bg-surface-gray text-text-main'
                    : 'text-text-secondary hover:bg-surface-base'
                "
              >
                <span>{{ option.label }}</span>
                <Check v-if="currentSort === option.value" :size="14" class="text-text-secondary" />
              </button>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>

  <div class="px-6 pb-6 pt-1">
    <div v-if="sortedBuildings.length > 0" class="flex flex-col gap-3">
      <BuildingCard
        v-for="building in sortedBuildings"
        :key="building.buildingId"
        :building="building"
        @click="openDetail(building.buildingId)"
      />
    </div>

    <div v-else class="flex flex-col items-center justify-center h-40 gap-2 text-center">
      <span class="text-[40px]">🏢</span>
      <p class="text-[14px] text-text-sub">해당하는 매물이 없습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUiStore } from '@/stores/uiStore.js';
import BuildingCard from './BuildingCard.vue';
import { Check, ChevronDown } from '@lucide/vue';
import { useClickOutside } from '@/hooks/useClickOutside.js';

const router = useRouter();
const uiStore = useUiStore();

// Mock
const mockBuildings = ref([
  {
    buildingId: '1121510700102120012',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102160011',
    lat: 37.5469847,
    lng: 127.0725513,
    jibunAddr: '서울특별시 광진구 화양동 216',
    roadAddr: null,
    bldNm: null,
    mainPurpsNm: '근린생활시설',
    grndFlr: 4,
    ugrndFlr: 1,
    useAprDay: '19950310',
    distance: 50,
  },
  {
    buildingId: '1121510700102180010',
    lat: 37.5461111,
    lng: 127.0711111,
    jibunAddr: '서울특별시 광진구 화양동 111',
    roadAddr: '서울특별시 광진구 능동로 111',
    bldNm: '화양빌딩',
    mainPurpsNm: '위락시설',
    grndFlr: 5,
    ugrndFlr: 2,
    useAprDay: '20150505',
    distance: 300,
  },
  {
    buildingId: '1121510700102120009',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102120008',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102120007',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102120006',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102120005',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102120004',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102120003',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102120002',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
  {
    buildingId: '1121510700102120001',
    lat: 37.5473051,
    lng: 127.073132,
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    bldNm: '메가타워',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '19781229',
    distance: 120,
  },
]);

const TABS = [
  { label: '주변 상가', value: 'nearby' },
  { label: '최근 본', value: 'recent' },
  { label: '찜한 상가', value: 'scrapped' },
];
const activeTab = ref('nearby');

const SORT_OPTIONS = [
  { label: '거리순', value: 'distance' },
  { label: '최신순', value: 'recent' },
];
const currentSort = ref('distance');

const isSortOpen = ref(false);
const sortDropdownRef = ref(null);

const currentSortLabel = computed(() => {
  const found = SORT_OPTIONS.find((o) => o.value === currentSort.value);
  return found ? found.label : '정렬 선택';
});

const selectSort = (val) => {
  currentSort.value = val;
  isSortOpen.value = false;
};

useClickOutside(sortDropdownRef, () => {
  isSortOpen.value = false;
});

// 리스트 정렬 및 필터링 로직 (api)
const sortedBuildings = computed(() => {
  if (activeTab.value !== 'nearby') return [];

  const list = [...mockBuildings.value];

  if (currentSort.value === 'distance') {
    return list.sort((a, b) => a.distance - b.distance);
  } else if (currentSort.value === 'recent') {
    return list.sort((a, b) => parseInt(b.useAprDay) - parseInt(a.useAprDay));
  }

  return list;
});

const openDetail = (buildingId) => {
  console.log(`클릭된 건물 ID: ${buildingId}`);

  uiStore.openBuildingDetail(buildingId);

  if (router) {
    router.push({ query: { buildingId } });
  } else {
    console.warn('라우터가 설정되지 않아 URL이 변경되지 않았습니다.');
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
