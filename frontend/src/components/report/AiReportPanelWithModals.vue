<script setup>
import { ref, computed } from 'vue';
import AiReportPanel from './AiReportPanel.vue';
import InsuranceModal from '@/components/finance/InsuranceModal.vue';
import LoanModal from '@/components/finance/LoanModal.vue';
import { useUiStore } from '@/stores/uiStore.js';
import {
  // getFloodInsuranceProducts,
  getBusinessInsuranceRiders,
  getMandatoryInsuranceItems,
} from '@/utils/insuranceFilters';
import { fetchLoanProducts, fetchInsuranceRidersByFactor } from '@/api/financeApi.js';

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

// const floodItems = computed(() =>
//   getFloodInsuranceProducts(reportData.value?.insuranceRidersByFactor),
// );

const businessItems = computed(() => {
  const mandatory = getMandatoryInsuranceItems(
    reportData.value?.disasterLiability,
    reportData.value?.fireLiability,
  );
  const riders = getBusinessInsuranceRiders(reportData.value?.insuranceRidersByFactor);
  return [...mandatory, ...riders];
});

// 보험 신청 URL — 풍수해/사업장종합 각각 별도 상품 페이지로 연결
const FLOOD_INSURANCE_APPLY_URL =
  'https://direct.kbinsure.co.kr/home/#/GL/DSF/GN_CM0101M/?pid=5110983&code=5703&utm_source=google&utm_medium=google_pc&utm_term=%EC%82%AC%EC%97%85%EC%9E%A5%EC%A2%85%ED%95%A9%EB%B3%B4%ED%97%98&utm_campaign=sa_bizFire&utm_content=51109835703&gclid=CjwKCAjwyuDTBhB-EiwANCQhLJaL56UhXSYTyJVm7DtlfOsVeyk_QfKlFKqkZbX-ynbDASB-kJcJQxoCzzYQAvD_BwE';
const BUSINESS_INSURANCE_APPLY_URL =
  'https://direct.kbinsure.co.kr/home/#/GL/BF/LT_CM0101M/?pid=5110983&code=5703&utm_source=google&utm_medium=google_pc&utm_term=%EC%82%AC%EC%97%85%EC%9E%A5%EC%A2%85%ED%95%A9%EB%B3%B4%ED%97%98&utm_campaign=sa_bizFire&utm_content=51109835703&gclid=CjwKCAjwyuDTBhB-EiwANCQhLJaL56UhXSYTyJVm7DtlfOsVeyk_QfKlFKqkZbX-ynbDASB-kJcJQxoCzzYQAvD_BwE';

// 배너 클릭 시 필터링된 items로 config를 구성해 uiStore에 위임
async function handleOpenInsurance(type) {
  if (type === 'flood') {
    // 진단 등급(CAUTION 여부)과 무관하게 항상 침수 관련 특약/상품을 직접 조회
    let items = [];
    try {
      const riders = await fetchInsuranceRidersByFactor('FLOOD');
      items = riders.filter((item) => item.coverageType === 'PRODUCT');
    } catch (err) {
      console.warn('[AiReportPanelWithModals] 풍수해보험 상품 조회 실패', err);
    }
    uiStore.openInsuranceModal({
      highlight: '풍수해보험',
      subtitle: '침수 피해 복구비 보장',
      items,
      ctaText: '사장님 맞춤 보험 상담 신청하기',
      applyUrl: FLOOD_INSURANCE_APPLY_URL,
    });
  } else if (type === 'business') {
    uiStore.openInsuranceModal({
      highlight: 'KB손해보험',
      subtitle: '진단 결과에 맞춰 필요한 상품과 특약을 골라봤어요.',
      items: businessItems.value,
      ctaText: '사장님 맞춤 보험 상담 신청하기',
      applyUrl: BUSINESS_INSURANCE_APPLY_URL,
    });
  }
}

// 대출 배너 클릭 시 API로 실제 상품 4종 조회 후 uiStore에 위임
async function handleOpenLoan() {
  try {
    const products = await fetchLoanProducts();
    uiStore.openLoanModal({ products });
  } catch (err) {
    console.warn('[AiReportPanelWithModals] 대출 상품 조회 실패', err);
  }
}

// 외부 링크를 새 탭으로 여는 공통 헬퍼
function openExternalLink(url) {
  window.open(url, '_blank', 'noopener,noreferrer');
}

// 보험 모달(풍수해/사업장종합 공통) 하단 배너 클릭 → 타입별 신청 페이지 이동 + 모달 닫기
function handleInsuranceSubmit() {
  const url = uiStore.insuranceModalConfig?.applyUrl;
  if (url) {
    openExternalLink(url);
  }
  uiStore.closeInsuranceModal();
}
// 대출 모달 - 상품별 '상세보기' 클릭 → 해당 상품의 KB스타뱅킹 페이지로 이동
function handleViewLoanDetail(product) {
  if (product?.detailUrl) {
    openExternalLink(product.detailUrl);
  }
}

// 대출 모달 - 인근 KB국민은행 영업점 찾기 배너 클릭
function handleFindBranch() {
  openExternalLink('https://map.naver.com/p/search/%EA%B5%AD%EB%AF%BC%EC%9D%80%ED%96%89');
}

// 대출 모달 - KB스타뱅킹 앱으로 신청하기 배너 클릭
function handleOpenStarbanking() {
  openExternalLink('https://obank.kbstar.com/quics?page=C110260');
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
    @view-detail="handleViewLoanDetail"
    @find-branch="handleFindBranch"
    @open-app="handleOpenStarbanking"
  />
</template>
