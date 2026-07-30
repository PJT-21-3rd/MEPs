<template>
  <div class="absolute bottom-10 right-6 z-20 flex flex-col gap-2" ref="controlsContainer">
    <MapControlButton title="내 위치" @click="mapStore.moveToMyLocation">
      <Navigation size="18px" />
    </MapControlButton>

    <div class="overflow-hidden rounded-xl bg-white shadow-md ring-1 ring-black/5">
      <button
        @click="mapStore.zoomIn"
        class="flex h-10 w-10 items-center justify-center hover:bg-neutral-50"
        title="확대"
      >
        <Plus size="18px" />
      </button>
      <div class="mx-auto h-px w-6 bg-neutral-200" />
      <button
        @click="mapStore.zoomOut"
        class="flex h-10 w-10 items-center justify-center hover:bg-neutral-50"
        title="축소"
      >
        <Minus size="18px" />
      </button>
    </div>

    <div class="relative flex items-center justify-end">
      <Transition
        enter-active-class="transition ease-out duration-200"
        enter-from-class="opacity-0 translate-x-3"
        enter-to-class="opacity-100 translate-x-0"
        leave-active-class="transition ease-in duration-150"
        leave-from-class="opacity-100 translate-x-0"
        leave-to-class="opacity-0 translate-x-3"
      >
        <div v-if="isLayerMenuOpen" class="absolute right-full mr-2 flex gap-2">
          <MapControlButton :active="activeBaseLayer === 'NORMAL'" @click="setBaseLayer('NORMAL')">
            <span class="text-xs font-semibold">일반</span>
          </MapControlButton>

          <MapControlButton
            :active="activeBaseLayer === 'SATELLITE'"
            @click="setBaseLayer('SATELLITE')"
          >
            <span class="text-xs font-semibold">위성</span>
          </MapControlButton>

          <MapControlButton :active="isCadastralActive" @click="toggleCadastral">
            <span class="flex items-start text-xs font-semibold leading-tight text-center">
              지적<br />편집도
            </span>
          </MapControlButton>
        </div>
      </Transition>

      <MapControlButton
        title="지도 레이어"
        :active="isLayerMenuOpen"
        @click="isLayerMenuOpen = !isLayerMenuOpen"
      >
        <Layers size="18px" />
      </MapControlButton>
    </div>

    <MapControlButton title="로드뷰" @click="handleRoadView">
      <Webcam size="18px" />
    </MapControlButton>
  </div>
</template>

<script setup>
import { useClickOutside } from '@/hooks/useClickOutside'; // 클릭감지
import { useMapStore } from '@/stores/mapStore';
import { Layers, Minus, Navigation, Plus, Webcam } from '@lucide/vue';
import { ref } from 'vue';
import MapControlButton from './MapControlButton.vue';

const mapStore = useMapStore();

const isLayerMenuOpen = ref(false);
const activeBaseLayer = ref('NORMAL'); // 기본 일반지도
const isCadastralActive = ref(false); // 지적도
const controlsContainer = ref(null);

let cadastralLayer = null;

const setBaseLayer = (type) => {
  activeBaseLayer.value = type;
  if (!mapStore.mapInstance) return;

  if (type === 'NORMAL') {
    mapStore.mapInstance.setMapTypeId(window.naver.maps.MapTypeId.NORMAL);
  } else if (type === 'SATELLITE') {
    mapStore.mapInstance.setMapTypeId(window.naver.maps.MapTypeId.SATELLITE);
  }
};

const toggleCadastral = () => {
  isCadastralActive.value = !isCadastralActive.value;
  if (!mapStore.mapInstance) return;

  if (isCadastralActive.value) {
    // on
    if (isCadastralActive.value) {
      cadastralLayer = new window.naver.maps.CadastralLayer();
    }
    cadastralLayer.setMap(mapStore.mapInstance);
  } else {
    // off
    if (cadastralLayer) {
      cadastralLayer.setMap(null);
    }
  }
};

const handleRoadView = () => {
  console.log('로드뷰');
};

useClickOutside(controlsContainer, () => {
  isLayerMenuOpen.value = false;
});
</script>
