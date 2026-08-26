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
        <SavedButton
          v-if="buildingDetail"
          :building-id="buildingDetail.buildingId"
          :initial-saved="buildingDetail.saved"
          @change="handleSaveChange"
        />
        <button
          @click="handleShare"
          class="rounded-full p-2 hover:bg-surface-base"
          aria-label="공유"
        >
          <Share2 :size="19" class="text-text-sub" />
        </button>
      </div>
    </header>

    <template v-if="uiStore.isDetailLoading">
      <BuildingDetailSkeleton />
    </template>

    <template v-else-if="!buildingDetail">
      <div class="flex-1 flex flex-col items-center justify-center gap-2 text-text-sub">
        <span class="text-[40px]">🏢</span>
        <p class="text-[14px]">건물 상세 정보를 찾을 수 없습니다.</p>
      </div>
    </template>

    <template v-else>
      <div ref="scrollRef" class="relative flex-1 overflow-y-auto">
        <!-- 로드뷰 -->
        <div class="px-4">
          <RoadViewImage :lat="roadViewLat" :lng="roadViewLng" />
        </div>
        <div ref="sentinelRef" class="h-px" />
        <!-- 인포 -->
        <BuildingInfoPannel
          ref="infoPanelRef"
          :buildingData="buildingDetail"
          :is-stuck="infoStuck"
        />
        <!-- 칩 -->
        <BuildingInfoChips :buildingData="buildingDetail" />
        <!-- 탭 + 토지/건물 -->
        <BuildingSpecs
          :buildingData="buildingDetail"
          :is-stuck="infoStuck"
          :header-height="headerHeight"
        />
      </div>
      <!-- 리포트 생성 버튼 -->
      <ReportCTAButton v-if="buildingDetail" @action="handleGenerateReport" />
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useToastStore } from '@/stores/toastStore';
import { useUiStore } from '@/stores/uiStore.js';
import { ChevronLeft, Share2 } from '@lucide/vue';
import RoadViewImage from './RoadViewImage.vue';
import BuildingInfoPannel from './BuildingInfoPannel.vue';
import BuildingInfoChips from './BuildingInfoChips.vue';
import BuildingSpecs from './BuildingSpecs.vue';
import ReportCTAButton from './ReportCTAButton.vue';
import SavedButton from '../common/SavedButton.vue';
import BuildingDetailSkeleton from './BuildingDetailSkeleton.vue';

const router = useRouter();
const uiStore = useUiStore();
const toastStore = useToastStore();

const scrollRef = ref(null);
const sentinelRef = ref(null);
const infoStuck = ref(false);
const infoPanelRef = ref(null);
const headerHeight = ref(0);
let observer = null;
let resizeObserver = null;

// 건물명/주소 div height 측정
const setupResizeObserver = () => {
  if (resizeObserver) resizeObserver.disconnect();

  const el = infoPanelRef.value?.$el;
  if (!el) return;

  resizeObserver = new ResizeObserver((entries) => {
    for (let entry of entries) {
      headerHeight.value = entry.target.getBoundingClientRect().height;
    }
  });
  resizeObserver.observe(el);
};

watch(infoPanelRef, () => {
  setupResizeObserver();
});

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
  // console.log('AI 리포트 패널 열기!');
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

const handleSaveChange = (newSavedState) => {
  if (uiStore.currentBuildingDetail) {
    uiStore.currentBuildingDetail.saved = newSavedState;
    // 찜 상태가 true가 되면 +1, false가 되면 -1
    uiStore.currentBuildingDetail.savedCnt += newSavedState ? 1 : -1;
  }
};

const handleShare = async () => {
  const bldName = buildingDetail.value?.bldNm || buildingDetail.value?.jibunAddr || '건물';
  const shareData = {
    title: 'MEPS 상가 안전 스캐너',
    text: `[MEPS] ${bldName}의 상세 정보와 안전 진단 리포트를 확인해보세요!`,
    url: window.location.href,
  };

  try {
    // TODO: 모바일 환경 등 Web Share API를 지원하는 경우 (네이티브 공유창 띄우기)
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

watch([sentinelRef, scrollRef], ([newSentinel, newScroll]) => {
  if (newSentinel && newScroll) {
    if (observer) observer.disconnect();

    observer = new IntersectionObserver(
      ([entry]) => {
        infoStuck.value = !entry.isIntersecting;
      },
      {
        root: newScroll,
        threshold: 0,
        rootMargin: '-1px 0px 0px 0px',
      },
    );
    observer.observe(newSentinel);
  }
});

onUnmounted(() => {
  if (observer) observer.disconnect();
  if (resizeObserver) resizeObserver.disconnect();
});
</script>
