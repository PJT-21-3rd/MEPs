<script setup>
import StatChipSkeleton from './StatChipSkeleton.vue';
</script>

<template>
  <!-- 카드 섹션 -->
  <div class="px-6 pb-5">
    <div class="relative overflow-hidden rounded-b-2xl bg-surface-blue px-5 pb-5 pt-1">
      <div class="relative mb-3.5 grid grid-cols-2 gap-2">
        <StatChipSkeleton v-for="i in 4" :key="i" />
      </div>

      <!-- 헤드 뉴스 라인 -->
      <div class="mb-3 flex items-center gap-2 rounded-lg bg-surface-red px-2.5 py-1.5 h-10">
        <span
          class="shrink-0 rounded bg-status-like px-1.5 py-0.5 text-[10px] font-bold text-white z-10"
        >
          NEWS
        </span>

        <div class="relative flex-1 h-6 overflow-hidden">
          <div class="h-3.5 w-3/4 rounded bg-surface-gray animate-pulse" />
        </div>
      </div>

      <!-- 동에 대한 설명 -->
      <div class="flex flex-col gap-2 mt-2">
        <div class="h-3 w-full rounded bg-surface-gray animate-pulse" />
        <div class="h-3 w-[90%] rounded bg-surface-gray animate-pulse" />
        <div class="h-3 w-[80%] rounded bg-surface-gray animate-pulse" />
        <div class="h-3 w-[60%] rounded bg-surface-gray animate-pulse" />
      </div>
    </div>
  </div>
</template>
