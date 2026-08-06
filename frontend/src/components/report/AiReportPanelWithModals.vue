<script setup>
import { ref, computed } from 'vue';
import AiReportPanel from './AiReportPanel.vue';
import InsuranceModal from '@/components/finance/InsuranceModal.vue';
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

// AiReportPanel이 실제로 로드한 reportData를 report-loaded 이벤트로 받아 동기화
// (initialReportData는 최초 렌더링 전 fallback 용도로만 사용)
const reportData = ref(props.initialReportData);

function handleReportLoaded(data) {
  reportData.value = data;
}
const insuranceModalType = ref(null); // 'flood' | 'business' | null

function handleOpenInsurance(type) {
  insuranceModalType.value = type;
}
function closeInsuranceModal() {
  insuranceModalType.value = null;
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

// AiReportWithModals.vue
const insuranceModalConfig = computed(() => {
  if (insuranceModalType.value === 'flood') {
    return {
      highlight: '풍수해보험',
      subtitle: '침수 피해 복구비 보장',
      items: floodItems.value,
    };
  }
  if (insuranceModalType.value === 'business') {
    return {
      highlight: 'KB손해보험',
      subtitle: '진단 결과에 맞춰 필요한 상품과 특약을 골라봤어요.',
      items: businessItems.value,
    };
  }
  return null;
});
</script>

<template>
  <AiReportPanel
    :building-id="buildingId"
    :building-name="buildingName"
    :initial-report-data="initialReportData"
    :initial-detail-report-data="initialDetailReportData"
    @close="$emit('close')"
    @open-insurance="handleOpenInsurance"
    @report-loaded="handleReportLoaded"
  />

  <InsuranceModal
    v-if="insuranceModalConfig"
    :highlight="insuranceModalConfig.highlight"
    :subtitle="insuranceModalConfig.subtitle"
    :items="insuranceModalConfig.items"
    cta-text="사장님 맞춤 보험 상담 신청하기"
    @close="closeInsuranceModal"
    @submit="closeInsuranceModal"
  />
</template>
