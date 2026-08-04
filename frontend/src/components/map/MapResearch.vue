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
import { useMapStore } from '@/stores/mapStore';
import { RotateCw } from '@lucide/vue';

const mapStore = useMapStore();

const handleReSearch = () => {
  const map = mapStore.mapInstance;

  if (map) {
    // 현재 화면에 보이는 지도의 BBox 가져옴
    const bounds = map.getBounds();

    // 네이버 지도 bounds 객체에서 남서쪽/북동쪽 좌표 추출
    const sw = bounds.getSW();
    const ne = bounds.getNE();

    console.log(`재검색 영역: 좌하단(${sw.lat()}, ${sw.lng()}) ~ 우상단(${ne.lat()}, ${ne.lng()})`);

    // TODO: 백엔드 API에 이 좌표를 보내서 상가/매물 데이터 새로 요청
  }

  mapStore.setMapMoved(false);
};
</script>
