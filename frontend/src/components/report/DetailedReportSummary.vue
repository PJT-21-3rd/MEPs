<script setup>
import { computed } from 'vue';
import { ChartColumn } from '@lucide/vue';

// #32 - 상세 리포트 상단 "종합 분석 요약" 카드
const props = defineProps({
  grade: {
    type: String,
    required: true,
  },
  aiReport: {
    type: String,
    required: true,
  },
});

// overallAiReport 여러 단락(빈 줄 구분)으로 올 가능성 고려
const paragraphs = computed(() => {
  if (!props.aiReport) return [];
  return props.aiReport.split(/\n\s*\n/).filter((p) => p.trim());
});
</script>

<template>
  <div class="mx-4 p-5 bg-surface-blue rounded-[16px] flex flex-col gap-1.5">
    <div class="flex items-center gap-1.5">
      <ChartColumn class="w-4 h-4 text-button-primary shrink-0" />
      <span class="text-[13px] font-Regular text-button-primary">종합 분석 요약</span>
    </div>
    <p
      v-for="(paragraph, idx) in paragraphs"
      :key="idx"
      class="text-[15px] text-text-main"
      :class="idx === 0 ? 'font-semibold' : 'font-regular'"
    >
      {{ paragraph }}
    </p>
  </div>
</template>
