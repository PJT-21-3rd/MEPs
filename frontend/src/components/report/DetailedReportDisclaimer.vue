<script setup>
import { computed } from 'vue';

// #32 - 상세 리포트 하단 면책 안내 문구

const today = computed(() => {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth() + 1;
  const day = now.getDate();
  return `${year}. ${month}. ${day}.`;
});
</script>

<template>
  <br />
  <p class="text-[12px] text-text-disabled text-center leading-relaxed px-8">
    본 리포트는 국토교통부, 행정안전부, 서울시 공공데이터
  </p>
  <p class="text-[12px] text-text-disabled text-center leading-relaxed px-8">
    기반으로 AI가 분석했습니다.
  </p>
  <p class="text-[12px] text-text-disabled text-center leading-relaxed px-8">
    분석 기준일: {{ today }}
  </p>
</template>
