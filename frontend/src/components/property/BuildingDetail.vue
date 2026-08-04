<template>
  <div class="flex flex-col h-full bg-white">
    <!-- 임시 페이지 -->
    <!-- Todos: -->
    <!-- 헤더 -->
    <div class="flex items-center gap-3 px-6 py-4 border-b border-neutral-100 shrink-0">
      <button
        @click="handleBack"
        class="flex h-9 w-9 items-center justify-center rounded-full hover:bg-neutral-100 transition-colors text-neutral-600"
        aria-label="뒤로가기"
      >
        <ArrowLeft class="h-5 w-5" />
      </button>
      <h2 class="text-lg font-bold text-neutral-900 truncate">
        {{ buildingDetail?.bldNm || buildingDetail?.jibunAddr || '건물 상세 정보' }}
      </h2>
    </div>
    <!-- 로드뷰 -->
    <div class="flex-1 overflow-y-auto hide-scrollbar p-6 space-y-6">
      <div class="relative h-48 w-full overflow-hidden rounded-2xl bg-neutral-100">
        <img
          src="https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?q=80&w=600&auto=format&fit=crop"
          alt="건물 사진"
          class="h-full w-full object-cover"
        />
        <div
          class="absolute left-4 top-4 flex items-center gap-1.5 rounded-lg bg-blue-600 px-3 py-1.5 shadow-md"
        >
          <ShieldCheck class="h-4 w-4 text-white" />
          <span class="text-xs font-bold text-white">안전 스코어 88점</span>
        </div>
      </div>
      <!-- 인포 -->
      <div>
        <span class="text-xs font-bold text-primary px-2 py-1 bg-primary/10 rounded-md">
          {{ buildingDetail?.mainPurpsNm || '근린생활시설' }}
        </span>
        <h3 class="mt-2 text-xl font-extrabold text-neutral-900">
          {{ buildingDetail?.bldNm || '건물명 미상' }}
        </h3>
        <p class="mt-1 text-sm text-neutral-500">
          {{ buildingDetail?.roadAddr || buildingDetail?.jibunAddr }}
        </p>
      </div>
      <!-- 탭 + 토지/건물 -->
      <div
        class="grid grid-cols-2 gap-3 p-4 rounded-xl bg-neutral-50 border border-neutral-100 text-xs"
      >
        <div>
          <span class="text-neutral-400">지상 / 지하</span>
          <p class="mt-0.5 font-bold text-neutral-800">
            지상 {{ buildingDetail?.grndFlr || 0 }}층 / 지하 {{ buildingDetail?.ugrndFlr || 0 }}층
          </p>
        </div>
        <div>
          <span class="text-neutral-400">사용승인일</span>
          <p class="mt-0.5 font-bold text-neutral-800">
            {{ buildingDetail?.useAprDay || '정보 없음' }}
          </p>
        </div>
      </div>
      <!-- 리포트 생성 버튼 -->
      <div class="p-4 rounded-2xl bg-gradient-to-r from-blue-500 to-blue-600 text-white shadow-md">
        <div class="flex items-center gap-2 text-xs font-semibold text-blue-100">
          <Sparkles class="h-4 w-4" /> KB국민은행 우대 혜택
        </div>
        <p class="mt-1 text-sm font-bold">안전 우수 상가 전용 창업대출 금리 우대 (최대 0.2%p)</p>
        <button
          class="mt-3 w-full py-2 bg-white text-blue-600 font-bold text-xs rounded-xl hover:bg-blue-50 transition-colors"
        >
          우대 금리 조건 확인하기
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useUiStore } from '@/stores/uiStore.js';
import { ArrowLeft, ShieldCheck, Sparkles } from '@lucide/vue';

const router = useRouter();
const uiStore = useUiStore();

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
