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

      <!-- 인포 -->

      <!-- 탭 + 토지/건물 -->

      <!-- 리포트 생성 버튼 -->
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUiStore } from '@/stores/uiStore.js';
import { ChevronLeft, Heart, Share2, Sparkles } from '@lucide/vue';

const router = useRouter();
const uiStore = useUiStore();

const isFavorite = ref(false);
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value;
};

// 임시 건물 상세 데이터 (추후 API 연동)
const buildingDetail = computed(() => {
  return {
    buildingId: uiStore.selectedBuildingId,
    bldNm: '메가타워',
    jibunAddr: '서울특별시 광진구 화양동 212',
    roadAddr: '서울특별시 광진구 광나루로 392',
    mainPurpsNm: '제2종근린생활시설',
    grndFlr: 12,
    ugrndFlr: 1,
    useAprDay: '1978년 12월 29일',
  };
});

// 뒤로가기 핸들러
const handleBack = () => {
  uiStore.closeBuildingDetail();
  // URL 쿼리 파라미터 제거
  router.push({ query: {} });
};
</script>
