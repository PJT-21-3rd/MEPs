<script setup>
import { Info, X } from '@lucide/vue';

const emit = defineEmits(['close']);
</script>

<template>
  <div
    class="inline-flex items-center gap-2 px-4 py-2.5 bg-primary text-white rounded-full shadow-lg"
  >
    <Info :size="16" class="text-secondary shrink-0" />
    <span class="text-[14px]">원하는 상가 건물을 클릭해 AI 안전 진단을 받아보세요!</span>
    <button @click="emit('close')" class="shrink-0 ml-1">
      <X :size="16" />
    </button>
  </div>
</template>
