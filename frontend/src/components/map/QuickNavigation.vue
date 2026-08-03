<template>
  <button
    @click.stop="openNav"
    class="absolute left-1/2 top-8 z-30 flex -translate-x-1/2 items-center gap-2.5 rounded-full bg-white/95 px-[28px] py-3 min-w-[282px] shadow-md backdrop-blur transition-transform"
  >
    <span class="text-[16px] tracking-tight text-text-sub font-semibold">{{ sido }}</span>
    <ChevronRight :size="18" class="text-text-sub" />
    <span class="text-[16px] tracking-tight text-primary font-semibold">{{ gu }}</span>
    <ChevronRight :size="18" class="text-text-sub" />
    <span class="text-[16px] tracking-tight text-primary font-semibold">{{ dong }}</span>
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
        <RegionColumn :items="Object.keys(REGIONS)" :selected="pSido" @pick="pickSido" />
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
import { ref, computed } from 'vue';
import { ChevronRight, X } from '@lucide/vue';
import RegionColumn from './RegionColumn.vue';
import { useClickOutside } from '@/hooks/useClickOutside.js';

const emit = defineEmits(['move']);
const REGIONS = {
  서울특별시: {
    강남구: ['역삼동', '대치동', '삼성동', '논현동'],
    광진구: ['화양동', '자양동', '구의동', '군자동'],
    성동구: ['성수동', '옥수동', '행당동'],
  },
};

const sido = ref('서울특별시');
const gu = ref('강남구');
const dong = ref('대치동');

const pSido = ref('');
const pGu = ref('');
const pDong = ref('');

const navOpen = ref(false);
const modalRef = ref(null);

const guList = computed(() => (pSido.value ? Object.keys(REGIONS[pSido.value] || {}) : []));
const dongList = computed(() =>
  pSido.value && pGu.value ? REGIONS[pSido.value][pGu.value] || [] : [],
);

const openNav = () => {
  // 모달을 열 때, 현재 확정된 지역을 탐색 상태로 복사
  pSido.value = sido.value;
  pGu.value = gu.value;
  pDong.value = dong.value;
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
  if (!pSido.value || !pGu.value || !pDong.value) {
    alert('읍/면/동까지 모두 선택해주세요.');
    return;
  }

  // 탐색 완료된 값을 실제 표시 값으로 덮어씌움
  sido.value = pSido.value;
  gu.value = pGu.value;
  dong.value = pDong.value;

  navOpen.value = false;

  // 부모(지도 컴포넌트)로 이동 이벤트 전달
  emit('move', { sido: sido.value, gu: gu.value, dong: dong.value });
};
</script>
