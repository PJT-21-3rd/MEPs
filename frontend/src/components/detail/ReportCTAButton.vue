<script setup>
import { ChevronRight, Zap } from '@lucide/vue';

defineEmits(['action']);
</script>

<template>
  <div class="absolute bottom-2 left-1/2 -translate-x-1/2 w-[calc(100%-40px)] z-20 bg-white">
    <button
      @click="$emit('action')"
      class="flex w-full items-center justify-center gap-1.5 rounded-2xl bg-primary py-4 text-[16px] text-white transition-transform hover:scale-[1.01] active:scale-95"
    >
      <Zap :size="18" class="text-secondary" /> AI 안전 진단 리포트 확인하기
      <ChevronRight :size="18" />
    </button>
  </div>
</template>
