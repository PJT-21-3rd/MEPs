<template>
  <Story title="Property / RoadViewImage (거리뷰)" icon="lucide:street-view">
    <Variant title="기본 렌더링 (건대입구역 인근)">
      <div class="flex justify-center p-8 bg-neutral-50">
        <div
          class="w-[400px] border border-neutral-200 shadow-lg rounded-2xl overflow-hidden bg-white"
        >
          <div class="p-4 border-b border-neutral-100">
            <h3 class="font-bold">건대입구 상가</h3>
            <p class="text-xs text-neutral-500">카메라가 건물을 딱 바라보는지 확인</p>
          </div>
          <RoadViewImage :lat="37.5473051" :lng="127.073132" />
        </div>
      </div>
    </Variant>

    <!-- 
    <Variant title="2. 동적 좌표 변경 테스트">
      <div class="flex justify-center p-8 bg-neutral-50">
        <div
          class="w-[400px] border border-neutral-200 shadow-lg rounded-2xl overflow-hidden bg-white"
        >
          <div class="p-4 border-b border-neutral-100">
            <h3 class="font-bold">실시간 위치 이동</h3>
            <p class="text-xs text-neutral-500">우측 Controls 패널에서 좌표를 조절해 보세요.</p>
          </div>

          <RoadViewImage :lat="dynamicLat" :lng="dynamicLng" />
        </div>
      </div>

      <template #controls>
        <HstNumber v-model="dynamicLat" title="위도 (Latitude)" :step="0.0001" />
        <HstNumber v-model="dynamicLng" title="경도 (Longitude)" :step="0.0001" />
      </template>
    </Variant> -->

    <Variant title="데이터 없음 (에러 화면)">
      <div class="flex justify-center p-8 bg-neutral-50">
        <div
          class="w-[400px] border border-neutral-200 shadow-lg rounded-2xl overflow-hidden bg-white"
        >
          <div class="p-4 border-b border-neutral-100">
            <h3 class="font-bold">거리뷰가 없는 곳 (바다)</h3>
            <p class="text-xs text-neutral-500">에러 스켈레톤 UI가 나오는지 확인</p>
          </div>

          <RoadViewImage :lat="37.5" :lng="125.0" />
        </div>
      </div>
    </Variant>
  </Story>
</template>

<script setup>
import { ref } from 'vue';
import RoadViewImage from './RoadViewImage.vue';

// Variant 2를 위한 동적 상태 (기본값: 롯데월드타워)
const dynamicLat = ref(37.5125);
const dynamicLng = ref(127.1027);
</script>
