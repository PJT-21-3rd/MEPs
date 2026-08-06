<template>
  <div class="flex flex-col h-full bg-white">
    <!-- 임시 페이지 -->
    <!-- Todos: -->
    <!-- 헤더 -->
    <header class="flex items-center justify-between px-4 py-3 border-t border-surface-base">
      <button
        @click="handleBack"
        class="flex items-center gap-1 rounded-lg px-2 py-1.5 text-[15px] text-text-main hover:bg-surface-base transition-colors"
        aria-label="뒤로가기"
      >
        <ChevronLeft :size="20" /> 목록
      </button>
      <div class="flex items-center gap-1">
        <button
          @click="toggleFavorite"
          class="rounded-full p-2 hover:bg-surface-base"
          aria-label="찜하기"
        >
          <Heart
            :size="20"
            :class="isFavorite ? 'fill-status-like text-status-like' : 'text-text-sub'"
          />
        </button>
        <button class="rounded-full p-2 hover:bg-surface-base" aria-label="공유">
          <Share2 :size="19" class="text-text-sub" />
        </button>
      </div>
    </header>

    <div class="relative flex-1 overflow-y-auto">
      <!-- 로드뷰 -->
      <div class="px-4">
        <RoadViewImage :lat="37.5023528" :lng="127.0259463" />
      </div>

      <!-- 인포 -->
      <div class="px-5 pt-4">
        <BuildingInfoPannel :buildingData="buildingDetail" />
      </div>
      <!-- 칩 -->
      <div class="px-5">
        <BuildingInfoChips :buildingData="buildingDetail" />
      </div>
      <!-- 탭 + 토지/건물 -->
      <BuildingSpecs :buildingData="buildingDetail" />
    </div>
    <!-- 리포트 생성 버튼 -->
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUiStore } from '@/stores/uiStore.js';
import { ChevronLeft, Heart, Share2, Sparkles } from '@lucide/vue';
import RoadViewImage from './RoadViewImage.vue';
import BuildingInfoPannel from './BuildingInfoPannel.vue';
import BuildingInfoChips from './BuildingInfoChips.vue';
import BuildingSpecs from './BuildingSpecs.vue';

const router = useRouter();
const uiStore = useUiStore();

const isFavorite = ref(false);
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value;
};

// 임시 건물 상세 데이터 (추후 API 연동)
const buildingDetail = computed(() => {
  return {
    buildingId: '1168010100102160000',
    pnu: '1168010100102160000',
    bldNm: '역삼 스타빌딩',
    mainPurps: '제2종근린생활시설',
    roadAddr: '서울특별시 강남구 테헤란로 152',
    jibunAddr: '서울특별시 강남구 역삼동 123-4',
    center: {
      type: 'Point',
      coordinates: [127.0366742, 37.5006373],
    },
    footprint: {
      type: 'MultiPolygon',
      coordinates: [
        [
          [
            [127.0365, 37.5007],
            [127.0368, 37.5007],
            [127.0368, 37.5005],
            [127.0365, 37.5005],
            [127.0365, 37.5007],
          ],
        ],
      ],
    },
    savedCnt: 12,
    land: {
      lndcgrCodeNm: '대',
      prposAreaNm: '일반상업지역',
      roadSideCodeNm: '광대소각',
      pblntfPclnd: 29040000,
    },
    detail: {
      strctCdNm: '철근콘크리트구조',
      grndFlr: 3,
      ugrndFlr: 1,
      hoCnt: 86,
      useAprDay: '20180101',
      elapsedYear: 8,
      violBdYn: 'N',
    },
    floors: [
      { flrGbNm: '지하', flrNoNm: '1층', mainPurpsNm: '제2종근린생활시설', etcPurps: '창고' },
      { flrGbNm: '지상', flrNoNm: '1층', mainPurpsNm: '제2종근린생활시설', etcPurps: '소매점' },
      { flrGbNm: '지상', flrNoNm: '2층', mainPurpsNm: '제2종근린생활시설', etcPurps: '일반음식점' },
      { flrGbNm: '지상', flrNoNm: '3층', mainPurpsNm: '제2종근린생활시설', etcPurps: '미용실' },
    ],
  };
});

// 뒤로가기 핸들러
const handleBack = () => {
  uiStore.closeBuildingDetail();
  // URL 쿼리 파라미터 제거
  router.push({ query: {} });
};
</script>
