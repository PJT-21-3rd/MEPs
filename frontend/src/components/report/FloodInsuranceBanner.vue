<script setup>
import { ChevronRight, ShieldCheck } from '@lucide/vue';

// #27에서 분리: 풍수해보험 배너만 담당 (의무보험 섹션이 그 아래 끼어야 해서 분리)
defineProps({
  floodOverlapNotice: {
    type: String,
    default: '',
  },
});
defineEmits(['open-insurance']);
</script>

<template>
  <div class="rounded-2xl bg-surface-base p-4 flex flex-col gap-4">
    <button
      type="button"
      class="w-full flex items-center gap-3 px-4 py-3 rounded-2xl bg-secondary text-left"
      @click="$emit('open-insurance', 'flood')"
    >
      <span class="w-9 h-9 shrink-0 rounded-[10px] bg-white flex items-center justify-center">
        <ShieldCheck class="w-[18px] h-[18px] text-secondary" />
      </span>
      <span class="flex-1 flex flex-col gap-0.5">
        <span class="text-[15px] font-bold text-primary">소상공인 풍수해보험</span>
        <span class="text-[13px] font-medium text-button-primary">침수 피해 복구비 보장</span>
      </span>
      <ChevronRight class="w-[18px] h-[18px] text-primary shrink-0" />
    </button>

    <p v-if="floodOverlapNotice" class="text-[14px] font-regular text-text-notice px-1">
      {{ floodOverlapNotice }}
    </p>
  </div>
</template>
