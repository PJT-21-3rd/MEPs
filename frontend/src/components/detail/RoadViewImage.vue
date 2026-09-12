<script setup>
import { ref, onMounted, watch } from 'vue';
import { ImageOff, Map } from '@lucide/vue';

const props = defineProps({
  lat: { type: Number, required: true },
  lng: { type: Number, required: true },
});

const panoContainer = ref(null);
const isLoaded = ref(false);
const hasError = ref(false);

let panorama = null;

// 좌표 사이의 방위각 계산
const calculateBearing = (lat1, lng1, lat2, lng2) => {
  const toRad = (val) => (val * Math.PI) / 180;
  const toDeg = (val) => (val * 180) / Math.PI;

  const rLat1 = toRad(lat1);
  const rLat2 = toRad(lat2);
  const dLng = toRad(lng2 - lng1);

  const y = Math.sin(dLng) * Math.cos(rLat2);
  const x = Math.cos(rLat1) * Math.sin(rLat2) - Math.sin(rLat1) * Math.cos(rLat2) * Math.cos(dLng);

  let bearing = Math.atan2(y, x);
  return (toDeg(bearing) + 360) % 360;
};

// 로드뷰 시선을 건물쪽으로 고정
const lookAtBuilding = () => {
  if (!panorama) return;

  // 로드뷰 좌표
  const panoLocation = panorama.getLocation();
  if (!panoLocation) return;
  const cameraCoord = panoLocation.coord;

  // 건물 좌표
  const targetLatLng = new window.naver.maps.LatLng(props.lat, props.lng);

  // 로드뷰 건물 간 방위각 계산
  const panAngle = calculateBearing(
    cameraCoord.lat(),
    cameraCoord.lng(),
    targetLatLng.lat(),
    targetLatLng.lng(),
  );

  // 시야 업데이트
  panorama.setPov({
    pan: panAngle, // 수평
    tilt: 10, // 수직
    fov: 90, // 시야각
  });
};

// 네이버 파노라마
const initPanorama = () => {
  if (!window.naver || !window.naver.maps || !window.naver.maps.Panorama) {
    console.error('네이버 파노라마 API가 없습니다.');
    hasError.value = true;
    return;
  }

  const targetLatLng = new window.naver.maps.LatLng(props.lat, props.lng);

  panorama = new window.naver.maps.Panorama(panoContainer.value, {
    position: targetLatLng,
    flightSpot: false,
    logoControl: false,
    zoomControl: false,
    panControl: true,
  });

  // 로딩완료 후 시야 변경
  window.naver.maps.Event.addListener(panorama, 'init', () => {
    isLoaded.value = true;

    if (!panorama.getPanoId()) {
      hasError.value = true;
      return;
    }

    lookAtBuilding();
  });
};

onMounted(() => {
  initPanorama();
});

// 건물 변경 시 위치를 갱신
watch(
  () => [props.lat, props.lng],
  () => {
    isLoaded.value = false;
    hasError.value = false;

    if (panorama) {
      panorama.setPosition(new window.naver.maps.LatLng(props.lat, props.lng));

      window.naver.maps.Event.once(panorama, 'pano_changed', () => {
        lookAtBuilding();
      });
    } else {
      initPanorama();
    }
  },
);
</script>

<template>
  <div class="relative h-[180px] overflow-hidden rounded-2xl bg-surface-base inset-0">
    <div
      v-if="!isLoaded && !hasError"
      class="absolute inset-0 flex flex-col items-center justify-center text-text-sub gap-2 z-10 bg-surface-base"
    >
      <Map :size="24" class="animate-bounce" />
      <span class="text-[13px]">이미지를 불러오는 중 ...</span>
    </div>
    <div
      v-if="hasError"
      class="absolute inset-0 flex flex-col items-center justify-center text-text-sub gap-2 z-10 bg-surface-base"
    >
      <ImageOff :size="24" />
      <span class="text-[13px]">해당 건물(위치)의 로드뷰는 제공되지 않습니다</span>
    </div>
    <div ref="panoContainer" class="w-full h-full pointer-events-auto"></div>
    <span
      class="absolute bottom-2.5 left-2.5 rounded-lg bg-black/55 px-2.5 py-1 text-[12px] text-white backdrop-blur"
    >
      로드뷰
    </span>
  </div>
</template>
