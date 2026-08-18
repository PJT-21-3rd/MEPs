<script setup>
import { Heart } from '@lucide/vue';
import { GRADE_META, getGradeByStatusCode } from '@/constants/reportConstants';

function gradeMeta(safetyGrade) {
  if (!safetyGrade) return null;
  return GRADE_META[getGradeByStatusCode(safetyGrade)];
}

defineProps({
  building: Object,
  order: Number,
});

const emit = defineEmits(['toggle', 'unlike']);
</script>

<template>
  <li
    @click="emit('toggle')"
    class="flex items-center gap-3 py-3.5 px-4 border rounded-xl cursor-pointer hover:bg-surface-blue transition-colors"
    :class="order ? 'border-primary bg-surface-blue' : 'border-surface-gray'"
  >
    <span
      class="shrink-0 w-4 h-4 rounded-full border-2 flex items-center justify-center text-[11px] font-bold"
      :class="order ? 'bg-primary border-primary text-white' : 'border-surface-gray'"
    >
      {{ order }}
    </span>

    <div class="flex-1 min-w-0 pr-6">
      <div class="flex items-center gap-1 min-w-0">
        <span class="text-[16px] font-bold truncate" :title="building.roadAddr">{{
          building.roadAddr
        }}</span>
        <span
          v-if="building.safetyGrade"
          class="shrink-0 text-[13px] px-[7px] py-0.5 rounded"
          :class="[gradeMeta(building.safetyGrade).badgeBg, gradeMeta(building.safetyGrade).text]"
        >
          {{ gradeMeta(building.safetyGrade).label }}
        </span>
        <span
          v-else
          class="shrink-0 text-[13px] px-[7px] py-0.5 rounded bg-surface-gray text-text-sub"
        >
          진단 전
        </span>
      </div>
      <p class="text-[13px] text-text-sub mt-[3px]">{{ building.bldNm || '건물명 없음' }}</p>
    </div>

    <div class="flex items-center gap-2.5 shrink-0">
      <span
        v-if="building.safetyScore !== null"
        class="text-[17px] font-bold"
        :class="gradeMeta(building.safetyGrade)?.text"
      >
        {{ building.safetyScore }}
      </span>
      <span v-else class="text-[17px] font-bold text-text-sub"> - </span>
      <button @click.stop="emit('unlike', building.buildingId)" class="cursor-pointer group">
        <Heart
          :size="18"
          fill="currentColor"
          class="text-status-like group-hover:fill-transparent transition-all"
        />
      </button>
    </div>
  </li>
</template>
