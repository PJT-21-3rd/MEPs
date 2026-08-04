<script setup>
import MandatoryInsuranceCard from './MandatoryInsuranceCard.vue';

// #181 - AI 안심 진단 리포트 하단에 상가 의무 보험 상품을 노출한다
// 상품 자체(재난배상책임보험/화재배상책임보험)는 고정
// 판별 배지는 상가 정보로 결정 - 추후 논의

defineProps({
  // { required: Boolean, description: String, evidenceTags: string[] }
  disasterLiability: {
    type: Object,
    required: true,
  },
  // { required: Boolean, description: String, evidenceTags: string[] }
  fireLiability: {
    type: Object,
    required: true,
  },
});
</script>

<template>
  <div class="flex flex-col gap-3">
    <p class="text-[16px] text-text-main">
      <span class="font-regular">이 상가의 </span>
      <span class="font-semibold">의무보험</span>
    </p>

    <MandatoryInsuranceCard
      name="재난배상책임보험"
      :required="disasterLiability.required"
      :description="disasterLiability.description"
      :evidence-tags="disasterLiability.evidenceTags"
    />

    <MandatoryInsuranceCard
      name="화재배상책임보험"
      :required="fireLiability.required"
      :description="fireLiability.description"
      :evidence-tags="fireLiability.evidenceTags"
    />
  </div>
</template>
