<script setup>
import { computed } from 'vue';
import MandatoryInsuranceCard from './MandatoryInsuranceCard.vue';

// #181 - AI 안심 진단 리포트 하단에 상가 의무 보험 상품을 노출한다
// 상품 자체(재난배상책임보험/화재배상책임보험)는 고정
// 판별 배지는 상가 정보로 결정 - 추후 논의

const props = defineProps({
  // { required: Boolean, description: String, evidenceTags: string[] }
  disasterLiability: {
    type: Object,
    required: null,
  },
  // { required: Boolean, description: String, evidenceTags: string[] }
  fireLiability: {
    type: Object,
    required: null,
  },
});

// API 응답 없을 때 카드 자체는 유지하되, 판정 배지만 "확인 중" 상태로 대체
function withFallback(liability) {
  return {
    required: liability?.required ?? null, // null이면 카드에서 "확인 중" 배지 처리
    description: liability?.description ?? '진단 결과를 확인하는 중이에요.',
    evidenceTags: liability?.evidenceTags ?? [],
  };
}

const disasterLiabilityDisplay = computed(() => withFallback(props.disasterLiability));
const fireLiabilityDisplay = computed(() => withFallback(props.fireLiability));
</script>

<template>
  <div class="flex flex-col gap-3">
    <p class="text-[16px] text-text-main">
      <span class="font-regular">이 상가의 </span>
      <span class="font-semibold">의무보험</span>
    </p>

    <MandatoryInsuranceCard
      name="재난배상책임보험"
      :required="disasterLiabilityDisplay.required"
      :description="disasterLiabilityDisplay.description"
      :evidence-tags="disasterLiabilityDisplay.evidenceTags"
    />

    <MandatoryInsuranceCard
      name="화재배상책임보험"
      :required="fireLiabilityDisplay.required"
      :description="fireLiabilityDisplay.description"
      :evidence-tags="fireLiabilityDisplay.evidenceTags"
    />
  </div>
</template>
