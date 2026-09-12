<script setup>
import MandatoryInsuranceCard from './MandatoryInsuranceCard.vue';

// #181 - AI 안심 진단 리포트 하단에 상가 의무 보험 상품을 노출한다
// 상품 자체(재난배상책임보험/화재배상책임보험)는 고정
// 판별 배지 항상 "필수" 고정
defineProps({
  // { description: String, evidenceTags: string[] }
  disasterLiability: {
    type: Object,
    default: null,
  },
  // { description: String, evidenceTags: string[] }
  fireLiability: {
    type: Object,
    default: null,
  },
});
</script>

<template>
  <div class="flex flex-col gap-3">
    <p class="text-[16px] text-text-main">
      <span class="font-regular">소상공인 </span>
      <span class="font-semibold">의무보험</span>
    </p>

    <MandatoryInsuranceCard
      name="재난배상책임보험"
      :description="
        disasterLiability?.description ?? '재난 및 안전관리 기본법에 따른 의무 가입 상품입니다.'
      "
      :evidence-tags="disasterLiability?.evidenceTags ?? []"
    />

    <MandatoryInsuranceCard
      name="화재배상책임보험"
      :description="
        fireLiability?.description ?? '화재 재해보상과 손해배상을 위한 의무 가입 상품입니다.'
      "
      :evidence-tags="fireLiability?.evidenceTags ?? []"
    />
  </div>
</template>
