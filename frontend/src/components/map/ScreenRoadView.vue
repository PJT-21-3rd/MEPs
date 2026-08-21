<script setup>
import { useUiStore } from '@/stores/uiStore';
import { X } from '@lucide/vue';
import { onMounted, ref, onUnmounted } from 'vue';

const uiStore = useUiStore();
const wrapper = ref(null);
const panoContainer = ref(null);
const miniMapContainer = ref(null);

let panorama = null;
let miniMap = null;
let miniMarker = null;
let resizeObserver = null;

const forceResizePanorama = () => {
  if (panorama && wrapper.value) {
    const width = wrapper.value.clientWidth;
    const height = wrapper.value.clientHeight;

    if (width > 0 && height > 0) {
      panorama.setSize(new window.naver.maps.Size(width, height));
    }
  }
};

onMounted(() => {
  const targetLatLng = new window.naver.maps.LatLng(
    uiStore.roadViewCoords.lat,
    uiStore.roadViewCoords.lng,
  );

  // 파노라마 생성
  panorama = new window.naver.maps.Panorama(panoContainer.value, {
    position: targetLatLng,
    aroundControl: false,
    logoControl: false,
    zoomControl: true,
  });

  setTimeout(() => {
    forceResizePanorama();
  }, 350);

  resizeObserver = new ResizeObserver(() => {
    forceResizePanorama();
  });
  resizeObserver.observe(wrapper.value);

  window.addEventListener('resize', forceResizePanorama);

  miniMap = new window.naver.maps.Map(miniMapContainer.value, {
    center: targetLatLng,
    zoom: 16,
    scaleControl: false,
    mapDataControl: false,
    zoomControl: false,
  });

  // 미니맵 위 내 위치 마커
  miniMarker = new window.naver.maps.Marker({
    position: targetLatLng,
    map: miniMap,
    icon: {
      content: `<div class="w-4 h-4 bg-red-500 border-2 border-white rounded-full shadow-md"></div>`,
      anchor: new window.naver.maps.Point(8, 8),
    },
  });

  // 위치 동기화
  window.naver.maps.Event.addListener(panorama, 'pano_changed', () => {
    const currentPos = panorama.getPosition();
    miniMap.setCenter(currentPos);
    miniMarker.setPosition(currentPos);
  });
});

onUnmounted(() => {
  if (resizeObserver) resizeObserver.disconnect();
  window.removeEventListener('resize', forceResizePanorama);
});
</script>

<template>
  <div ref="wrapper" class="absolute inset-0 z-30 flex bg-black overflow-hidden">
    <div ref="panoContainer" class="w-full h-full relative"></div>
    <div
      class="absolute bottom-8 right-8 w-[280px] h-[140px] rounded-2xl border-[3px] border-white shadow-2xl overflow-hidden z-40 pointer-events-auto"
    >
      <div ref="miniMapContainer" class="w-full h-full bg-surface-gray"></div>
    </div>
    <button
      @click="uiStore.closeRoadViewModal"
      class="absolute top-6 right-6 z-50 flex items-center justify-center w-12 h-12 bg-white/20 hover:bg-white/40 backdrop-blur-md rounded-full text-white transition-all shadow-md pointer-events-auto cursor-pointer"
    >
      <X :size="20" />
    </button>
  </div>
</template>
