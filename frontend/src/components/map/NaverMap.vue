<template>
  <div id="map" ref="mapContainer" class="w-full h-full bg-surface-gray"></div>
</template>

<script setup>
import { useMapStore } from '@/stores/mapStore';
import { ref, onMounted } from 'vue';

const mapContainer = ref(null);
const mapStore = useMapStore();
let timeOut = null;

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

  const handleMapStart = () => {
    if (timeOut) clearTimeout(timeOut);
    mapStore.setMapMoved(false);
  };

  // 🌟 지도의 모든 움직임이 완전히 멈췄을 때 (마우스, 줌, 빠른 이동 모두 포함)
  const handleMapIdle = () => {
    if (timeOut) clearTimeout(timeOut);

    timeOut = setTimeout(() => {
      mapStore.setMapMoved(true); // 1초 뒤에 버튼 띄우기
    }, 800);
  };

  window.naver.maps.Event.addListener(map, 'dragstart', handleMapStart);
  window.naver.maps.Event.addListener(map, 'idle', handleMapIdle);
});
</script>
