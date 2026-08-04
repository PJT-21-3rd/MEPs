<script setup>
import { ShieldCheck } from '@lucide/vue';

defineProps({
  name: { type: String, required: true },
  required: { type: Boolean, required: true },
  description: { type: String, required: true },
  evidenceTags: { type: Array, default: () => [] },
});
</script>

<template>
  <div
    class="p-4 rounded-2xl border"
    :class="required ? 'bg-surface-red border-border-red' : 'bg-white border-surface-gray'"
  >
    <div class="flex items-center gap-1.5 mb-3">
      <ShieldCheck class="w-[18px] h-[18px] text-secondary shrink-0" />
      <span class="flex-1 text-[16px] font-regular text-text-main">{{ name }}</span>
      <span
        class="px-2.5 py-1 rounded-full text-[13px] font-regular shrink-0"
        :class="required ? 'bg-status-like text-white' : 'bg-badge-neutral text-text-muted'"
      >
        {{ required ? '필수' : '해당없음' }}
      </span>
    </div>

    <p class="text-[14px] font-regular text-text-secondary mb-2.5">{{ description }}</p>

    <div v-if="evidenceTags.length" class="flex flex-wrap gap-1.5">
      <span
        v-for="tag in evidenceTags"
        :key="tag"
        class="px-2 py-1 rounded-full bg-surface-gray border-white text-[9.5px] text-text-badge font-regular"
      >
        근거 · {{ tag }}
      </span>
    </div>
  </div>
</template>
