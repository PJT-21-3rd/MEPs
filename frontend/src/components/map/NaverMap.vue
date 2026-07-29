<template>
  <div id="map" ref="mapContainer" class="w-full h-full bg-surface-gray"></div>
</template>

<script setup>
import { useMapStore } from '@/stores/mapStore';
import { ref, onMounted } from 'vue';

const mapContainer = ref(null);
const mapStore = useMapStore();

let map = null;

onMounted(() => {
  if (!window.naver || !window.naver.maps) {
    console.error('네이버 지도 API를 불러올 수 없습니다.');
    return;
  }

  // 지도 초기 옵션 설정
  const mapOptions = {
    center: new window.naver.maps.LatLng(37.4979, 127.0276),
    zoom: 15,
    zoomControl: false,
  };

  const map = new window.naver.maps.Map(mapContainer.value, mapOptions);
  mapStore.setMapInstance(map);
});
</script>
