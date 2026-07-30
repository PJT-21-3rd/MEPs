<!-- src/components/property/CommercialAiBriefing.vue -->
<script setup>
import { Sparkles } from '@lucide/vue';
</script>

<template>
  <div class="rounded-2xl bg-gradient-to-br from-surface-blue to-[#f5fbff] p-5">
    <!-- 브리핑 헤더 -->
    <div class="mb-3 flex items-center gap-2">
      <span
        class="flex items-center gap-1 rounded-full bg-button-primary px-2.5 py-1 text-[12px] text-white"
      >
        <Sparkles :size="12" /> AI 브리핑
      </span>
      <p class="text-[18px] tracking-tight text-neutral-900">대치동</p>
      <p class="text-[14px] text-neutral-400">강남구</p>
    </div>

    <!-- 4그리드 카드 섹션 -->
    <div class="mb-3.5 grid grid-cols-2 gap-2"></div>

    <!-- 헤드 뉴스 라인 -->

    <!-- 동에 대한 설명 -->
  </div>
</template>
