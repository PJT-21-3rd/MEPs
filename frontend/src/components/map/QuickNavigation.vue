<template>
  <button
    @click.stop="openNav"
    class="absolute left-1/2 top-8 z-30 flex -translate-x-1/2 items-center gap-2.5 rounded-full bg-white/95 px-[28px] py-3 min-w-[282px] shadow-md backdrop-blur transition-transform"
  >
    <span class="text-[16px] tracking-tight text-text-sub font-semibold">{{ currentSido }}</span>
    <ChevronRight :size="18" class="text-text-sub" />
    <span class="text-[16px] tracking-tight text-primary font-semibold">{{ currentGu }}</span>
    <ChevronRight :size="18" class="text-text-sub" />
    <span class="text-[16px] tracking-tight text-primary font-semibold">{{ currentDong }}</span>
  </button>

  <Transition
    enter-active-class="transition-all duration-300 ease-out"
    enter-from-class="opacity-0 -translate-y-4 scale-95"
    enter-to-class="opacity-100 translate-y-0 scale-100"
    leave-active-class="transition-all duration-200 ease-in"
    leave-from-class="opacity-100 translate-y-0 scale-100"
    leave-to-class="opacity-0 -translate-y-4 scale-95"
  >
    <div
      v-if="navOpen"
      ref="modalRef"
      @mousedown.stop
      class="pointer-events-auto absolute left-1/2 top-24 z-40 w-[400px] -translate-x-1/2 overflow-hidden rounded-2xl bg-white shadow-[0_24px_60px_-12px_rgba(0,0,0,0.3)]"
    >
      <div class="flex items-center justify-between p-4">
        <p class="text-[18px] font-bold tracking-tight text-text-main">지역 바로가기</p>
        <button
          @click="closeNav"
          class="rounded-full p-1 text-text-sub transition-colors hover:bg-surface-gray hover:text-text-secondary"
          aria-label="닫기"
        >
          <X :size="24" />
        </button>
      </div>

      <div class="grid grid-cols-3 bg-white">
        <RegionColumn :items="Object.keys(regionsData)" :selected="pSido" @pick="pickSido" />
        <RegionColumn :items="guList" :selected="pGu" @pick="pickGu" divider />
        <RegionColumn :items="dongList" :selected="pDong" @pick="setPDong" divider />
      </div>

      <button
        @click="confirmNav"
        class="mt-2 w-full bg-primary py-3.5 text-[14px] font-semibold tracking-tight text-white transition-colors hover:bg-blue-700"
      >
        {{ pSido }} {{ pGu }} {{ pDong }} 으로 이동
      </button>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ChevronRight, X } from '@lucide/vue';
import { useClickOutside } from '@/hooks/useClickOutside.js';
import { useMapStore } from '@/stores/mapStore.js';
import { useUiStore } from '@/stores/uiStore.js';
import { useToastStore } from '@/stores/toastStore';
import { fetchRegionList } from '@/api/map.js';
import RegionColumn from './RegionColumn.vue';

const mapStore = useMapStore();
const uiStore = useUiStore();
const toastStore = useToastStore();
const emit = defineEmits(['move']);

const regionsData = ref({ 서울특별시: {} });

const currentSido = computed(() => uiStore.hjdBriefingData?.sidoName || '서울특별시');
const currentGu = computed(() => uiStore.hjdBriefingData?.sggName || '광진구');
const currentDong = computed(() => uiStore.hjdBriefingData?.hjdName || '화양동');

const pSido = ref('');
const pGu = ref('');
const pDong = ref('');

const navOpen = ref(false);
const modalRef = ref(null);

const loadRegions = async () => {
  try {
    const responseData = await fetchRegionList();
    const regionsArray = responseData.regions || responseData;

    const parsedData = { 서울특별시: {} };

    regionsArray.forEach((item) => {
      const guName = item.sggName;
      const dongName = item.hjdNm;

      if (!parsedData['서울특별시'][guName]) {
        parsedData['서울특별시'][guName] = [];
      }
      parsedData['서울특별시'][guName].push(dongName);
    });

    regionsData.value = parsedData;
  } catch (error) {
    toastStore.showToast('지역 데이터를 불러오는데 실패했습니다.');
  }
};

onMounted(() => {
  loadRegions();
});

const guList = computed(() =>
  pSido.value ? Object.keys(regionsData.value[pSido.value] || {}) : [],
);
const dongList = computed(() =>
  pSido.value && pGu.value ? regionsData.value[pSido.value][pGu.value] || [] : [],
);

const openNav = () => {
  // 모달을 열 때, 현재 확정된 지역을 탐색 상태로 복사
  pSido.value = currentSido.value;
  pGu.value = currentGu.value;
  pDong.value = currentDong.value;
  navOpen.value = true;
};

const closeNav = () => {
  navOpen.value = false;
};

// 외부 클릭 시 모달 닫기
useClickOutside(modalRef, closeNav);

const pickSido = (val) => {
  pSido.value = val;
  pGu.value = '';
  pDong.value = '';
};

const pickGu = (val) => {
  pGu.value = val;
  pDong.value = '';
};

const setPDong = (val) => {
  pDong.value = val;
};

const confirmNav = () => {
  // 탐색 완료된 값을 실제 표시 값으로 덮어씌움
  navOpen.value = false;

  const targetAddress = `${pSido.value} ${pGu.value} ${pDong.value}`;
  if (!window.naver || !window.naver.maps.Service) {
    console.error('Geocoding 서비스가 준비되지 않았습니다.');
    toastStore.showToast('지도 주소 변환 서비스에 오류가 있습니다.'); // 에러 상황도 토스트 처리
    return;
  }

  // 주소를 좌표로 변환
  window.naver.maps.Service.geocode({ query: targetAddress }, (status, response) => {
    if (status === window.naver.maps.Service.Status.ERROR) {
      return console.error('주소 변환 API 에러');
    }

    if (response.v2.meta.totalCount === 0) {
      return toastStore.showToast('해당 지역의 좌표를 찾을 수 없습니다.');
    }

    const item = response.v2.addresses[0];
    const targetLatLng = new window.naver.maps.LatLng(item.y, item.x);

    // mapStore에 저장된 네이버 지도 객체를 꺼내서 애니메이션 이동 (morph)
    const map = mapStore.mapInstance;
    if (map) {
      map.morph(targetLatLng, 15, {
        duration: 800,
        easing: 'easeOutCubic',
      });
    } else {
      console.error('지도 객체가 아직 초기화되지 않았습니다.');
    }
  });
};
</script>
