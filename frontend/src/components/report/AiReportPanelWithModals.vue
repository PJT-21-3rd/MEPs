<script setup>
import { ref, computed } from 'vue';
import AiReportPanel from './AiReportPanel.vue';
import InsuranceModal from '@/components/finance/InsuranceModal.vue';
import LoanModal from '@/components/finance/LoanModal.vue';
import { useUiStore } from '@/stores/uiStore.js';
import {
  getFloodInsuranceProducts,
  getBusinessInsuranceRiders,
  getRequiredMandatoryInsurance,
} from '@/utils/insuranceFilters';

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
});

defineEmits(['close']);

const uiStore = useUiStore();

// AiReportPanel이 실제로 로드한 reportData를 report-loaded 이벤트로 받아 동기화
// (initialReportData는 최초 렌더링 전 fallback 용도로만 사용)
const reportData = ref(props.initialReportData);

function handleReportLoaded(data) {
  reportData.value = data;
}

const floodItems = computed(() =>
  getFloodInsuranceProducts(reportData.value?.insuranceRidersByFactor),
);

const businessItems = computed(() => {
  const mandatory = getRequiredMandatoryInsurance(
    reportData.value?.disasterLiability,
    reportData.value?.fireLiability,
  );
  const riders = getBusinessInsuranceRiders(reportData.value?.insuranceRidersByFactor);
  return mandatory ? [mandatory, ...riders] : riders;
});

// 배너 클릭 시 필터링된 items로 config를 구성해 uiStore에 위임
function handleOpenInsurance(type) {
  if (type === 'flood') {
    uiStore.openInsuranceModal({
      highlight: '풍수해보험',
      subtitle: '침수 피해 복구비 보장',
      items: floodItems.value,
      ctaText: '사장님 맞춤 보험 상담 신청하기',
    });
  } else if (type === 'business') {
    uiStore.openInsuranceModal({
      highlight: 'KB손해보험',
      subtitle: '진단 결과에 맞춰 필요한 상품과 특약을 골라봤어요.',
      items: businessItems.value,
      ctaText: '사장님 맞춤 보험 상담 신청하기',
    });
  }
}

// 대출 상품 4종 — 진단 결과와 무관하게 고정 노출 (BE 확정 전 임시값)
// TODO: 4개 상품명·금리 BE 확정되면 constants 파일로 분리
const LOAN_PRODUCTS = [
  {
    category: '창업 자금',
    name: 'KB사장님+ 마이너스통장',
    rateText: '연 최저 3.8%~',
    description: '우량 상권 입점 예정 소상공인을 위한 창업 자금 대출입니다.',
  },
  {
    category: '신용대출',
    name: 'KB소상공인 신용대출',
    rateText: '연 최저 4.2%~',
    description: '소상공인 신용등급에 따라 우대금리를 제공하는 대출 상품입니다.',
  },
  {
    category: '보증서 대출',
    name: 'KB소상공인 보증서대출(온택트)',
    rateText: '연 최저 3.5%~',
    description: '신용보증재단 보증서 기반 비대면 대출 상품입니다.',
  },
  {
    category: '셀러론',
    name: 'KB셀러론',
    rateText: '연 최저 5.0%~',
    description: '온라인 셀러를 위한 매출 기반 신속 대출 상품입니다.',
  },
];

// 대출 배너 클릭 시 uiStore에 위임 (등급과 무관하게 항상 동일한 상품 4종)
function handleOpenLoan() {
  uiStore.openLoanModal({ products: LOAN_PRODUCTS });
}

// 외부 링크를 새 탭으로 여는 공통 헬퍼
function openExternalLink(url) {
  window.open(url, '_blank', 'noopener,noreferrer');
}

// 보험 모달(풍수해/사업장종합 공통) 하단 배너 클릭 → KB손해보험 안내 페이지 이동 + 모달 닫기
function handleInsuranceSubmit() {
  openExternalLink('https://www.kbinsure.co.kr/main.ec?mdmn=0101');
  uiStore.closeInsuranceModal();
}

// 대출 모달 - 인근 KB국민은행 영업점 찾기 배너 클릭
function handleFindBranch() {
  openExternalLink('https://map.naver.com/p/search/%EA%B5%AD%EB%AF%BC%EC%9D%80%ED%96%89');
}

// 대출 모달 - KB스타뱅킹 앱으로 신청하기 배너 클릭
function handleOpenStarbanking() {
  openExternalLink('https://zloan.kbstar.com/quics?page=opzloan');
}
</script>

<template>
  <AiReportPanel
    :building-id="buildingId"
    :building-name="buildingName"
    :initial-report-data="initialReportData"
    :initial-detail-report-data="initialDetailReportData"
    @close="$emit('close')"
    @open-insurance="handleOpenInsurance"
    @open-loan="handleOpenLoan"
    @report-loaded="handleReportLoaded"
  />

  <InsuranceModal
    v-if="uiStore.insuranceModalConfig"
    v-bind="uiStore.insuranceModalConfig"
    @close="uiStore.closeInsuranceModal"
    @submit="handleInsuranceSubmit"
  />

  <LoanModal
    v-if="uiStore.loanModalConfig"
    v-bind="uiStore.loanModalConfig"
    @close="uiStore.closeLoanModal"
    @view-detail="(product) => console.log('상세보기', product)"
    @find-branch="handleFindBranch"
    @open-app="handleOpenStarbanking"
  />
</template>
