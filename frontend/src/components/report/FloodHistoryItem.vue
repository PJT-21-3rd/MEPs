<script setup>
import StatusBadge from '@/components/common/StatusBadge.vue';
import { WavesHorizontal } from '@lucide/vue';

//  #25 - 침수이력 한줄 요약

defineProps({
  status: {
    type: String,
    required: true,
    validator: (v) => ['safe', 'good', 'warning', 'danger'].includes(v),
  },
  summary: {
    type: String,
    required: true,
  },
  mode: {
    type: String,
    default: 'summary',
    validator: (v) => ['summary', 'detail'].includes(v),
  },
  detail: {
    type: Object,
    default: null,
  },
});
</script>

<template>
  <div class="p-4 border border-surface-gray rounded-xl">
    <div class="flex items-center gap-2 mb-2">
      <WavesHorizontal class="w-4 h-4 text-text-sub" />
      <span class="flex-1 text-sm font-semibold text-text-main">침수이력</span>
      <StatusBadge :status="status" />
    </div>

    <p class="text-sm text-text-secondary">"{{ summary }}"</p>

    <div v-if="mode === 'detail' && status === 'warning'" class="mt-3">
      <div v-if="detail.insurance" class="bg-surface-base rounded-lg p-3">
        <p class="text-sm font-semibold text-text-main mb-0.5">{{ detail.insurance.name }}</p>
        <p class="text-xs text-text-sub">{{ detail.insurance.description }}</p>
      </div>
    </div>
  </div>
</template>
