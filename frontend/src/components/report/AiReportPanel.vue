<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { GRADE_META } from '@/constants/reportConstants.js';
import ScoreGauge from './ScoreGauge.vue';
import AiBriefingCard from './AiBriefingCard.vue';
import DiagnosticFactorList from './DiagnosticFactorList.vue';
import { fetchReportData } from '@/api/reportApi';
import { X, ArrowLeft, ChevronRight, FileText, Zap } from '@lucide/vue';

const props = defineProps({
  buildingId: {
    type: [String, Number],
    required: true,
  },
  // 상세 뷰 헤더의 건물명 표시용 (예: "동양빌딩")
  buildingName: {
    type: String,
    default: '',
  },
  // Histoire 테스트 전용: 값이 있으면 API 호출 없이 이 데이터로 초기화한다
  initialReportData: {
    type: Object,
    default: null,
  },
  // // Histoire 테스트 전용: true면 API 호출 없이 로딩 상태를 계속 유지한다
  forceLoading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['close']); // 패널 닫기

const currentView = ref('summary');
const isLoading = ref(true);
const hasError = ref(false);
const reportData = ref(null);

// detail 헤더의 등급·점수 배지용 (예: "주의 · 74점")
const gradeMeta = computed(() => {
  if (!reportData.value) return null;
  return GRADE_META[reportData.value.grade];
});

// 리포트 데이터 불러오기 (initialReportData 있으면 API 호출 스킵)
async function loadReport() {
  if (props.forceLoading) {
    isLoading.value = true;
    hasError.value = false;
    currentView.value = 'summary';
    return; // 로딩 상태 유지
  }

  if (props.initialReportData) {
    reportData.value = props.initialReportData;
    currentView.value = 'summary';
    hasError.value = false;
    isLoading.value = false;
    return;
  }

  isLoading.value = true;
  hasError.value = false;
  currentView.value = 'summary';
  try {
    reportData.value = await fetchReportData(props.buildingId);
  } catch {
    hasError.value = true;
  } finally {
    isLoading.value = false;
  }
}

onMounted(loadReport);
watch(() => props.buildingId, loadReport); // 다른 건물 클릭 시 리포트 다시 조회

function openDetail() {
  currentView.value = 'detail'; // 상세 진단 리포트로 전환
}

function backToSummary() {
  currentView.value = 'summary'; // 요약(AI 안심 진단 리포트)로 복귀
}

function handleClose() {
  emit('close'); // 패널 닫기 -> 건물 상세로 복귀
}
</script>

<template>
  <div class="w-full h-full bg-white flex flex-col overflow-y-auto">
    <!-- 헤더: 안심 진단일 때만 닫기, 상세 뷰일 때만 뒤로가기 버튼 -->
    <div class="flex items-center gap-4 px-4 py-4.5 border-b border-surface-gray">
      <button
        v-if="currentView === 'detail'"
        type="button"
        class="text-text-sub hover:text-text-main"
        aria-label="이전 리포트로 돌아가기"
        @click="backToSummary"
      >
        <arrow-left class="w-4 h-4" />
      </button>

      <!-- summary 뷰: 아이콘 + 타이틀 한 줄 -->
      <p
        v-if="currentView === 'summary'"
        class="flex-1 flex items-center gap-1.5 text-[17px] font-Regular text-text-main"
      >
        <Zap class="w-4 h-4 text-secondary shrink-0" />
        AI 안심 진단 리포트
      </p>

      <!-- detail 뷰: 건물명 + 타이틀 2줄 + 우측 등급/점수 배지 -->
      <div v-else class="flex-1 flex items-center justify-between gap-2">
        <div class="flex flex-col gap-0.5">
          <span class="text-[13px] text-text-sub">{{ buildingName }}</span>
          <span class="text-[16px] font-regular text-text-main"
            >4대 근거 전체 상세 진단 리포트</span
          >
        </div>

        <span
          v-if="gradeMeta"
          class="px-3 py-1 rounded-full text-xs font-semibold shrink-0"
          :class="[gradeMeta.badgeBg, gradeMeta.text]"
        >
          {{ gradeMeta.label }} · {{ reportData.score }}점
        </span>
      </div>

      <!-- summary 뷰일 때만 닫기 버튼 표시 -->
      <button
        v-if="currentView === 'summary'"
        type="button"
        class="text-text-sub hover:text-text-main"
        aria-label="리포트 패널 닫기"
        @click="handleClose"
      >
        <x class="w-4 h-4" />
      </button>
    </div>

    <!-- 로딩 중 -->
    <div v-if="isLoading" class="flex-1 flex items-center justify-center text-sm text-text-sub">
      리포트를 불러오는 중이에요...
    </div>

    <!-- summary 뷰: 스코어 + 브리핑 + 진단근거 4개 + 상세보기 CTA -->
    <div v-else-if="currentView === 'summary'" class="flex-1 flex flex-col gap-5 px-4 py-2">
      <div class="flex flex-col gap-2">
        <ScoreGauge :score="reportData.score" :grade="reportData.grade" />
        <AiBriefingCard :loading="false" :briefing="reportData.overallBriefing" />
      </div>

      <DiagnosticFactorList :items="reportData.dangerItems" mode="summary" />

      <button
        type="button"
        class="w-full py-4 rounded-2xl bg-button-primary text-white text-sm font-semibold flex flex-row items-center justify-center gap-2"
        @click="openDetail"
      >
        <FileText class="w-4 h-4 shrink-0" />
        <span>4대 근거 전체 상세 진단 리포트 보기</span>
        <ChevronRight class="w-4 h-4 shrink-0" />
      </button>
    </div>

    <!-- detail 뷰: 상세 진단 리포트 (추후 구현 예정, 지금은 placeholder) -->
    <div
      v-else-if="currentView === 'detail'"
      class="flex-1 flex items-center justify-center text-sm text-text-sub"
    >
      상세 진단 리포트는 추후 구현 예정
    </div>
  </div>
</template>
