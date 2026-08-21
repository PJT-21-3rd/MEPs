<script setup>
import { ref } from 'vue';
import { logEvent } from 'histoire/client';

import QuickNavigation from './QuickNavigation.vue';
import RegionColumn from './RegionColumn.vue';

const mockGuList = ['강남구', '서초구', '송파구', '광진구', '성동구', '용산구'];
const selectedGu = ref('광진구');

const handlePickGu = (val) => {
  selectedGu.value = val;
  // Histoire의 'Events' 탭에 클릭 기록을 남깁니다.
  logEvent('컬럼 아이템 클릭 (pick)', val);
};

const handleRegionMove = (regionData) => {
  // 모달에서 'OO동으로 이동' 버튼을 눌렀을 때 발생하는 이벤트를 로깅합니다.
  const { sido, gu, dong } = regionData;
  const address = `${sido} ${gu} ${dong}`;

  logEvent('📍 최종 지도 이동 명령 (move)', address);
};
</script>

<template>
  <Story title="Map / Region Navigation" icon="lucide:map-pin">
    <Variant title="1. 단일 컬럼 (RegionColumn)">
      <div class="p-8 bg-neutral-100 flex justify-center">
        <div class="bg-white rounded-3xl shadow-lg w-[200px] py-4">
          <RegionColumn :items="mockGuList" :selected="selectedGu" @pick="handlePickGu" />
        </div>
      </div>

      <template #controls>
        <HstText v-model="selectedGu" title="선택된 항목" />
      </template>
    </Variant>

    <Variant title="2. 전체 빠른 이동 모달 (RegionNavModal)">
      <div
        class="relative w-full h-[700px] bg-[#eef2f6] overflow-hidden flex items-center justify-center"
      >
        <div
          class="absolute inset-0 opacity-20 bg-[url('https://www.transparenttextures.com/patterns/cubes.png')]"
        ></div>
        <p class="text-neutral-400 font-bold text-xl z-0">🗺️ Naver Map Rendering Area</p>

        <QuickNavigation @move="handleRegionMove" />
      </div>
    </Variant>
  </Story>
</template>
