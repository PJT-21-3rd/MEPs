<template>
  <Transition
    enter-active-class="transition-opacity duration-300 ease-out"
    enter-from-class="opacity-0"
    enter-to-class="opacity-100"
    leave-active-class="transition-opacity duration-100 ease-in"
    leave-from-class="opacity-100"
    leave-to-class="opacity-0"
  >
    <button
      v-if="mapStore.isMapMoved"
      @click="handleReSearch"
      class="absolute z-20 overflow-hidden bottom-17 left-1/2 -translate-x-1/2 pointer-events-auto flex items-center gap-2 rounded-xl bg-primary px-5 py-3 shadow-[0_4px_16px_rgba(0,0,0,0.15)]"
    >
      <RotateCw :size="18" class="text-white" />
      <span class="text-[15px] font-semibold text-white">현 지도에서 검색</span>
    </button>
  </Transition>
</template>

<script setup>
import { useUiStore } from '@/stores/uiStore';
import { useMapStore } from '@/stores/mapStore';
import { fetchNearbyBuildings } from '@/api/building';
import { RotateCw } from '@lucide/vue';

const mapStore = useMapStore();
const uiStore = useUiStore();

const handleReSearch = async () => {
  const map = mapStore.mapInstance;
  if (!map) return;

  const bounds = map.getBounds();
  const sw = bounds.getSW();
  const ne = bounds.getNE();
  const currentZoom = map.getZoom();

  console.log(
    `[재검색] 영역: 좌하단(${sw.lat()}, ${sw.lng()}) ~ 우상단(${ne.lat()}, ${ne.lng()}) | 줌: ${currentZoom}`,
  );

  try {
    const data = await fetchNearbyBuildings(sw.lat(), sw.lng(), ne.lat(), ne.lng(), currentZoom);

    uiStore.setBuildingsData(data);
  } catch (error) {
    console.error('재검색 중 오류 발생:', error);
    alert('데이터를 불러오는데 실패했습니다.');
  } finally {
    mapStore.setMapMoved(false);
  }
};
</script>
