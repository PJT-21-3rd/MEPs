<template>
  <div class="flex flex-col h-full bg-white">
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
        <button
          @click="handleShare"
          class="rounded-full p-2 hover:bg-surface-base"
          aria-label="공유"
        >
          <Share2 :size="19" class="text-text-sub" />
        </button>
      </div>
    </header>

    <div
      v-if="uiStore.isDetailLoading"
      class="flex-1 flex flex-col items-center justify-center gap-3 text-text-sub"
    >
      <div
        class="w-8 h-8 border-3 border-button-primary border-t-transparent rounded-full animate-spin"
      ></div>
      <p class="text-[14px]">건물 상세 정보를 불러오는 중입니다...</p>
    </div>

    <div
      v-else-if="!buildingDetail"
      class="flex-1 flex flex-col items-center justify-center gap-2 text-text-sub"
    >
      <span class="text-[40px]">🏢</span>
      <p class="text-[14px]">건물 상세 정보를 찾을 수 없습니다.</p>
    </div>

    <div v-else class="relative flex-1 overflow-y-auto">
      <!-- 로드뷰 -->
      <div class="px-4">
        <RoadViewImage :lat="roadViewLat" :lng="roadViewLng" />
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
    <ReportCTAButton v-if="buildingDetail" @action="handleGenerateReport" />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useToastStore } from '@/stores/toastStore';
import { useUiStore } from '@/stores/uiStore.js';
import { ChevronLeft, Heart, Share2 } from '@lucide/vue';
import RoadViewImage from './RoadViewImage.vue';
import BuildingInfoPannel from './BuildingInfoPannel.vue';
import BuildingInfoChips from './BuildingInfoChips.vue';
import BuildingSpecs from './BuildingSpecs.vue';
import ReportCTAButton from './ReportCTAButton.vue';

const router = useRouter();
const uiStore = useUiStore();
const toastStore = useToastStore();

const isFavorite = ref(false);
const toggleFavorite = () => {
  isFavorite.value = !isFavorite.value;
};

// 건물 상세 데이터
const buildingDetail = computed(() => uiStore.currentBuildingDetail);

const roadViewLat = computed(() => {
  const coords = buildingDetail.value?.center?.coordinates;
  return coords && coords.length >= 2 ? coords[1] : 37.5445;
});
const roadViewLng = computed(() => {
  const coords = buildingDetail.value?.center?.coordinates;
  return coords && coords.length >= 2 ? coords[0] : 127.0716;
});

const handleGenerateReport = () => {
  console.log('AI 리포트 패널 열기!');
  uiStore.openReport();
};

// 뒤로가기 핸들러
const handleBack = () => {
  uiStore.closeBuildingDetail();
  // URL 쿼리 파라미터 제거
  const query = { ...router.currentRoute.value.query };
  delete query.buildingId;
  router.push({ query: {} });
};

const handleShare = async () => {
  const bldName = buildingDetail.value?.bldNm || buildingDetail.value?.jibunAddr || '건물';
  const shareData = {
    title: 'MEPS 상가 안전 스캐너',
    text: `[MEPS] ${bldName}의 상세 정보와 안전 진단 리포트를 확인해보세요!`,
    url: window.location.href,
  };

  try {
    await navigator.clipboard.writeText(window.location.href);
    toastStore.showToast('링크가 클립보드에 복사되었습니다.');
  } catch (error) {
    if (error.name !== 'AbortError') {
      try {
        await navigator.clipboard.writeText(window.location.href);
        toastStore.showToast('링크가 클립보드에 복사되었습니다.');
      } catch (err) {
        toastStore.showToast('공유하기를 지원하지 않는 브라우저입니다.');
      }
    }
  }
};
</script>
