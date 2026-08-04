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

const lookAtBuilding = () => {
  if (!panorama) return;

  // 1. 도로 위에 있는 거리뷰 카메라의 '실제' 좌표
  const panoLocation = panorama.getLocation();
  if (!panoLocation) return;
  const cameraCoord = panoLocation.coord;

  // 2. 사용자가 보려고 하는 '건물'의 좌표
  const targetLatLng = new window.naver.maps.LatLng(props.lat, props.lng);

  // 3. 카메라 -> 건물 방향으로의 각도 계산
  const panAngle = calculateBearing(
    cameraCoord.lat(),
    cameraCoord.lng(),
    targetLatLng.lat(),
    targetLatLng.lng(),
  );

  // 4. 시야(Pov) 업데이트
  panorama.setPov({
    pan: panAngle, // 건물 쪽으로 휙 돌리기
    tilt: 10, // 건물이 보이도록 카메라를 위로 살짝(10도) 치켜들기
    fov: 90, // 시야각
  });
};

// 네이버 파노라마를 띄우는 핵심 로직
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

  // 로딩이 완료되면 시선을 돌림
  window.naver.maps.Event.addListener(panorama, 'init', () => {
    isLoaded.value = true;

    if (!panorama.getPanoId()) {
      hasError.value = true;
      return;
    }

    // 로딩 직후 건물 바라보기 실행!
    lookAtBuilding();
  });
};

onMounted(() => {
  initPanorama();
});

// 건물이 바뀌면(Props 변경) 위치를 갱신
watch(
  () => [props.lat, props.lng],
  () => {
    isLoaded.value = false;
    hasError.value = false;

    if (panorama) {
      panorama.setPosition(new window.naver.maps.LatLng(props.lat, props.lng));

      // 위치 이동(비동기)이 완료된 후 딱 한 번만(once) 시선을 다시 돌려줌
      // (이후 사용자가 마우스로 드래그하며 동네를 둘러볼 때는 개입하지 않기 위함)
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
  <div class="relative h-[180px] overflow-hidden rounded-2xl bg-surface-base">
    <div
      v-if="!isLoaded && !hasError"
      class="absolute inset-0 flex flex-col items-center justify-center text-neutral-400 gap-2 z-10 bg-neutral-100"
    >
      <Map :size="24" class="animate-bounce" />
      <span class="text-[13px]">이미지를 불러오는 중 ...</span>
    </div>
    <div
      v-if="hasError"
      class="absolute inset-0 flex flex-col items-center justify-center text-neutral-400 gap-2 z-10 bg-neutral-100"
    >
      <ImageOff :size="24" />
      <span class="text-[13px]">해당 건물의 이미지는 제공되지 않습니다</span>
    </div>
    <div ref="panoContainer" class="w-full h-full pointer-events-auto"></div>
    <span
      class="absolute bottom-2.5 left-2.5 rounded-lg bg-black/55 px-2.5 py-1 text-[12px] text-white backdrop-blur"
    >
      로드뷰
    </span>
  </div>
</template>
