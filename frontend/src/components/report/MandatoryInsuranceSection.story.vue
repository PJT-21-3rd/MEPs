<script setup>
import MandatoryInsuranceSection from './MandatoryInsuranceSection.vue';
</script>

<template>
  <Story
    title="AI 리포트 컴포넌트/MandatoryInsuranceSection"
    :layout="{ type: 'single', iframe: false }"
  >
    <Variant title="기본 (재난 필수 + 화재 해당없음)">
      <div style="width: 400px" class="px-4 py-4 bg-white">
        <MandatoryInsuranceSection
          :disaster-liability="{
            required: true,
            description: '1층 음식점 의무 가입 · 미가입 시 과태료 최대 300만 원',
            evidenceTags: ['지상 1층', '업종 카페'],
          }"
          :fire-liability="{
            required: false,
            description: '다중이용업소 미해당 · 바닥면적 기준 미달',
            evidenceTags: [],
          }"
        />
      </div>
    </Variant>

    <Variant title="둘 다 필수">
      <div style="width: 400px" class="px-4 py-4 bg-white">
        <MandatoryInsuranceSection
          :disaster-liability="{
            required: true,
            description: '1층 음식점 의무 가입 · 미가입 시 과태료 최대 300만 원',
            evidenceTags: ['지상 1층', '업종 카페'],
          }"
          :fire-liability="{
            required: true,
            description: '다중이용업소 해당 · 의무 가입 대상',
            evidenceTags: ['다중이용업소'],
          }"
        />
      </div>
    </Variant>

    <Variant title="둘 다 해당없음">
      <div style="width: 400px" class="px-4 py-4 bg-white">
        <MandatoryInsuranceSection
          :disaster-liability="{
            required: false,
            description: '1층 미해당 · 의무 가입 대상 아님',
            evidenceTags: [],
          }"
          :fire-liability="{
            required: false,
            description: '다중이용업소 미해당 · 바닥면적 기준 미달',
            evidenceTags: [],
          }"
        />
      </div>
    </Variant>
  </Story>
</template>
