<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import ReportBanners from './ReportBanners.vue';
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
  buildingName: {
    type: String,
    default: '',
  },
  initialReportData: {
    type: Object,
    default: null,
  },
  forceLoading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['close', 'open-insurance', 'open-loan']);
const router = useRouter();

const currentView = ref('summary');
const isLoading = ref(true);
const reportData = ref(null);

const gradeMeta = computed(() => {
  if (!reportData.value) return null;
  return GRADE_META[reportData.value.grade];
});

// 침수이력이 주의 등급일 때만 풍수해보험 배너 아래 중복 안내 문구 노출
const floodOverlapNotice = computed(() => {
  if (reportData.value?.dangerItems?.flood?.status === 'warning') {
    return '침수이력 추천 특약의 "풍수해 특약"과 보장이 중복돼요';
  }
  return '';
});

async function loadReport() {
  if (props.forceLoading) {
    isLoading.value = true;
    currentView.value = 'summary';
    return;
  }

  if (props.initialReportData) {
    reportData.value = props.initialReportData;
    currentView.value = 'summary';
    isLoading.value = false;
    return;
  }

  isLoading.value = true;
  currentView.value = 'summary';
  try {
    reportData.value = await fetchReportData(props.buildingId);
  } catch {
    // API 실패(존재하지 않는 buildingId 등) -> 404 페이지로 이동
    // TODO: 404 라우트 이름/경로는 router/index.js에 NotFoundView 등록 후 확정 필요
    router.push({ name: 'NotFound' });
    return;
  } finally {
    isLoading.value = false;
  }
}

onMounted(loadReport);
watch(() => props.buildingId, loadReport);

function openDetail() {
  currentView.value = 'detail';
}

function backToSummary() {
  currentView.value = 'summary';
}

function handleClose() {
  emit('close');
}
</script>

<template>
  <div class="w-full h-full bg-white flex flex-col overflow-y-auto">
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

      <p
        v-if="currentView === 'summary'"
        class="flex-1 flex items-center gap-1.5 text-[17px] font-Regular text-text-main"
      >
        <Zap class="w-4 h-4 text-secondary shrink-0" />
        AI 안심 진단 리포트
      </p>

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

    <!-- summary 뷰 -->
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

      <ReportBanners
        :flood-overlap-notice="floodOverlapNotice"
        @open-insurance="(type) => $emit('open-insurance', type)"
        @open-loan="$emit('open-loan')"
      />
    </div>

    <!-- detail 뷰: 상세 진단 리포트 (#32에서 구현 예정) -->
    <div
      v-else-if="currentView === 'detail'"
      class="flex-1 flex items-center justify-center text-sm text-text-sub"
    >
      상세 진단 리포트는 추후 구현 예정
    </div>
  </div>
</template>
