<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import FloodInsuranceBanner from './FloodInsuranceBanner.vue';
import MandatoryInsuranceSection from './MandatoryInsuranceSection.vue';
import ReportBanners from './ReportBanners.vue';
import OfflineAgentCard from './OfflineAgentCard.vue';
import DetailedReportSummary from './DetailedReportSummary.vue';
import DetailedReportDisclaimer from './DetailedReportDisclaimer.vue';
import { GRADE_META } from '@/constants/reportConstants.js';
import ScoreGauge from './ScoreGauge.vue';
import AiBriefingCard from './AiBriefingCard.vue';
import DiagnosticFactorList from './DiagnosticFactorList.vue';
import { fetchReportData, fetchDetailedReportData } from '@/api/reportApi';
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
  initialDetailReportData: {
    type: Object,
    default: null,
  },
  forceLoading: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['close', 'open-insurance', 'open-loan', 'report-loaded']);
const router = useRouter();

const currentView = ref('summary');
const isLoading = ref(true);
const reportData = ref(null);

// #32: 상세 리포트는 summary와 별도 API라, 상세보기 클릭 시점에 지연 로딩
const detailReportData = ref(null);
const isDetailLoading = ref(false);
const detailHasError = ref(false);

const gradeMeta = computed(() => {
  if (!reportData.value) return null;
  return GRADE_META[reportData.value.grade];
});

const floodOverlapNotice = computed(() => {
  if (reportData.value?.dangerItems?.flood?.status === 'warning') {
    return '침수이력 추천 특약의 "풍수해 특약"과 보장이 중복돼요';
  }
  return '';
});

// basic 응답(status/summary)과 상세 응답(aiReport)을 병합해서 -> mergedDetailItems
// DiagnosticFactorList(detail 모드)에 넘길 최종 데이터를 만듦
const mergedDetailItems = computed(() => {
  if (!reportData.value || !detailReportData.value) return null;
  const merged = {};
  Object.keys(reportData.value.dangerItems).forEach((key) => {
    merged[key] = {
      ...reportData.value.dangerItems[key],
      aiReport: detailReportData.value.dangerItems[key]?.aiReport ?? '',
    };
  });
  return merged;
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
    emit('report-loaded', reportData.value);
    return;
  }

  isLoading.value = true;
  currentView.value = 'summary';
  try {
    reportData.value = await fetchReportData(props.buildingId);
    emit('report-loaded', reportData.value);
  } catch {
    // TODO: 404 라우트 이름/경로는 router/index.js에 NotFoundView 등록 후 확정 필요
    router.push({ name: 'NotFound' });
    return;
  } finally {
    isLoading.value = false;
  }
}

async function loadDetailReport() {
  if (detailReportData.value) return; // 이미 로드했으면 재조회 안 함

  if (props.initialDetailReportData) {
    detailReportData.value = props.initialDetailReportData;
    return;
  }

  isDetailLoading.value = true;
  detailHasError.value = false;
  try {
    detailReportData.value = await fetchDetailedReportData(props.buildingId);
  } catch (err) {
    console.warn('[AiReportPanel] 상세 리포트 조회 실패', err);
    detailHasError.value = true;
  } finally {
    isDetailLoading.value = false;
  }
}

onMounted(loadReport);
watch(
  () => props.buildingId,
  () => {
    detailReportData.value = null; // 다른 건물이면 상세 리포트도 다시 불러와야 함
    loadReport();
  },
);

function openDetail() {
  currentView.value = 'detail';
  loadDetailReport();
}

function backToSummary() {
  currentView.value = 'summary';
}

function handleClose() {
  emit('close');
}

// #29: 리포트 패널 스크롤이 바닥에 닿으면 공인중개사 카드를 지도 위에 노출
// 지도 정확한 좌표를 모르는 상태라, 화면 우측 지도 영역쯤을 대략적인 fixed 좌표로 배치
// TODO: 실제 3단 레이아웃 폭 확정되면 좌표 조정 필요
const scrollContainer = ref(null);
const showAgentCard = ref(false);

function handleScroll() {
  const el = scrollContainer.value;
  if (!el) return;

  // 이미 떠있으면 재판단 안 함 (한번 뜨면 유지)
  if (showAgentCard.value) return;

  const isScrollable = el.scrollHeight > el.clientHeight;
  const isNearBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 24;

  if (isScrollable && isNearBottom) {
    showAgentCard.value = true;
  }
}
const mockAgent = {
  name: '김민준',
  company: 'KB부동산개발법인(주)',
  phone: '010-0000-0000',
};
</script>

<template>
  <div
    ref="scrollContainer"
    class="w-full h-full bg-white flex flex-col overflow-y-auto"
    @scroll="handleScroll"
  >
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
    <div v-else-if="currentView === 'summary'" class="flex-1 flex flex-col gap-5 px-5 py-2">
      <div class="flex flex-col gap-2">
        <ScoreGauge :score="reportData.score" :grade="reportData.grade" />
        <AiBriefingCard :loading="false" :briefing="reportData.overallBriefing" />
      </div>

      <!-- 기본 리포트일 때  -->
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

      <FloodInsuranceBanner
        :flood-overlap-notice="floodOverlapNotice"
        @open-insurance="(type) => $emit('open-insurance', type)"
      />

      <MandatoryInsuranceSection
        :disaster-liability="reportData.disasterLiability"
        :fire-liability="reportData.fireLiability"
      />

      <ReportBanners
        @open-insurance="(type) => $emit('open-insurance', type)"
        @open-loan="$emit('open-loan')"
      />
    </div>

    <!-- detail 뷰 -->
    <Transition
      enter-active-class="transition duration-600 ease-out"
      enter-from-class="opacity-0 translate-y-6"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-6"
    >
      <div v-if="currentView === 'detail'" class="flex-1 flex flex-col gap-5 px-1 pb-2 pt-5">
        <div
          v-if="isDetailLoading"
          class="flex-1 flex items-center justify-center text-sm text-text-sub"
        >
          상세 리포트를 불러오는 중이에요...
        </div>

        <div
          v-else-if="detailHasError"
          class="flex-1 flex flex-col items-center justify-center gap-3 text-sm text-text-sub"
        >
          <p>상세 리포트를 불러오지 못했어요. 다시 시도해주세요.</p>
          <button
            type="button"
            class="px-4 py-2 rounded-lg bg-primary text-white text-sm font-semibold"
            @click="loadDetailReport"
          >
            다시 시도
          </button>
        </div>

        <template v-else-if="mergedDetailItems">
          <DetailedReportSummary
            :grade="reportData.grade"
            :ai-report="detailReportData.overallAiReport"
          />
          <div class="mt-2">
            <DiagnosticFactorList :items="mergedDetailItems" mode="detail" />
          </div>
          <div class="mt-5">
            <DetailedReportDisclaimer />
          </div>
        </template>
      </div>
    </Transition>

    <!-- #29: 공인중개사 안내 카드 (지도 영역 위 fixed 배치, 좌표는 임시값) -->
    <Transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="opacity-0 translate-y-4"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-4"
    >
      <OfflineAgentCard
        v-if="showAgentCard"
        class="fixed bottom-12 right-60 z-20"
        dong-name="역삼동"
        :agent="mockAgent"
      />
    </Transition>
  </div>
</template>
