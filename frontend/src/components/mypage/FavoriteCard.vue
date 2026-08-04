<script setup>
import { Heart } from '@lucide/vue';

const props = defineProps({
  building: Object,
  order: Number,
});

const emit = defineEmits(['toggle']);

function gradeClass(grade) {
  if (grade === '안전') return 'bg-grade-safe text-text-safe';
  if (grade === '양호') return 'bg-grade-good text-text-good';
  if (grade === '주의') return 'bg-grade-warn text-text-warn';
  return '';
}

function scoreColorClass(score) {
  if (score >= 90) return 'text-text-safe';
  if (score >= 80) return 'text-text-good';
  if (score >= 70) return 'text-text-warn';
  return '';
}
</script>

<template>
  <li
    @click="emit('toggle')"
    class="flex items-center gap-3 py-3.5 px-4 border rounded-xl cursor-pointer"
    :class="order ? 'border-primary bg-surface-blue' : 'border-surface-gray'"
  >
    <span
      class="shrink-0 w-5 h-5 rounded-full border-2 flex items-center justify-center text-[11px] font-bold"
      :class="order ? 'bg-primary border-primary text-white' : 'border-surface-gray'"
    >
      {{ order }}
    </span>

    <div class="flex-1 min-w-0">
      <div class="flex items-center gap-1.5">
        <span class="text-[15px] font-bold">{{ building.name }}</span>
        <span class="text-[11px] px-[7px] py-0.5 rounded" :class="gradeClass(building.grade)">
          {{ building.grade }}
        </span>
      </div>
      <p class="text-xs text-text-sub mt-[3px]">{{ building.address }}</p>
    </div>

    <div class="flex items-center gap-2.5 shrink-0">
      <span class="text-[17px] font-bold" :class="scoreColorClass(building.score)">
        {{ building.score }}
      </span>
      <Heart :size="18" fill="currentColor" class="text-status-like" />
    </div>
  </li>
</template>
