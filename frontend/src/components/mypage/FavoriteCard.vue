<script setup>
import { Heart } from '@lucide/vue';
import { getGradeByScore, GRADE_META } from '@/constants/reportConstants';

function gradeMeta(score) {
  return GRADE_META[getGradeByScore(score)];
}

defineProps({
  building: Object,
  order: Number,
});

const emit = defineEmits(['toggle']);
</script>

<template>
  <li
    @click="emit('toggle')"
    class="flex items-center gap-3 py-3.5 px-4 border rounded-xl cursor-pointer"
    :class="order ? 'border-primary bg-surface-blue' : 'border-surface-gray'"
  >
    <span
      class="shrink-0 w-4 h-4 rounded-full border-2 flex items-center justify-center text-[11px] font-bold"
      :class="order ? 'bg-primary border-primary text-white' : 'border-surface-gray'"
    >
      {{ order }}
    </span>

    <div class="flex-1 min-w-0">
      <div class="flex items-center gap-1.5">
        <span class="text-[13px] font-bold truncate">{{ building.address }}</span>
        <span
          class="text-[13px] px-[7px] py-0.5 rounded"
          :class="[gradeMeta(building.score).badgeBg, gradeMeta(building.score).text]"
        >
          {{ gradeMeta(building.score).label }}
        </span>
      </div>
      <p class="text-xs text-text-sub mt-[3px]">{{ building.name }}</p>
    </div>

    <div class="flex items-center gap-2.5 shrink-0">
      <span class="text-[17px] font-bold" :class="gradeMeta(building.score).text">
        {{ building.score }}
      </span>
      <Heart :size="18" fill="currentColor" class="text-status-like" />
    </div>
  </li>
</template>
