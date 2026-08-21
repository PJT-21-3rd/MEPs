<script setup>
import { computed } from 'vue';
import { Heart } from '@lucide/vue';
import { GRADE_META, getGradeByStatusCode } from '@/constants/reportConstants';
import { useRouter } from 'vue-router';
import { formatShortAddress } from '@/utils/formatters';

const props = defineProps({
  building: Object,
  order: Number,
});

const emit = defineEmits(['toggle', 'unlike']);

const router = useRouter();

const isDiagnosed = computed(() => props.building.safetyScore !== null);

function gradeMeta(safetyGrade) {
  if (!safetyGrade) return null;
  return GRADE_META[getGradeByStatusCode(safetyGrade)];
}

function handleClick() {
  if (!isDiagnosed.value) return;
  emit('toggle');
}

function goToDetail() {
  router.push({ path: '/', query: { buildingId: props.building.buildingId } });
}
</script>

<template>
  <li
    @click="handleClick"
    class="flex items-center gap-3 py-3.5 px-4 bg-surface-base border rounded-xl transition-colors"
    :class="[
      isDiagnosed ? 'cursor-pointer hover:bg-surface-blue' : 'cursor-default',
      order
        ? 'border-primary bg-surface-blue'
        : isDiagnosed
          ? 'border-text-sub/30'
          : 'border-text-sub/10',
    ]"
  >
    <span
      class="shrink-0 w-4 h-4 rounded-full border-2 flex items-center justify-center text-[11px] font-bold"
      :class="
        order
          ? 'bg-primary border-primary text-white'
          : isDiagnosed
            ? 'border-text-sub/30'
            : 'border-text-sub/10'
      "
    >
      {{ order }}
    </span>

    <div class="flex-1 min-w-0 pr-3">
      <div class="flex items-center gap-1 min-w-0">
        <span class="text-[15px] tracking-tight truncate" :title="building.roadAddr">
          {{ formatShortAddress(building.jibunAddr, building.roadAddr, building.bldNm) }}
        </span>
        <!-- 진단됨: 등급 배지 -->
        <span
          v-if="building.safetyGrade"
          class="shrink-0 text-[12px] px-[7px] py-0.5 rounded"
          :class="[gradeMeta(building.safetyGrade).badgeBg, gradeMeta(building.safetyGrade).text]"
        >
          {{ gradeMeta(building.safetyGrade).label }}
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
      <button
        v-else
        @click.stop="goToDetail"
        class="shrink-0 text-[14px] px-[7px] py-1 rounded bg-surface-gray text-primary cursor-pointer hover:bg-surface-blue transition-colors"
      >
        진단하기
      </button>
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
