<script setup>
import { computed, ref } from 'vue';
import { Heart, Loader2 } from '@lucide/vue';
import { GRADE_META, getGradeByStatusCode } from '@/constants/reportConstants';
import { fetchReportData } from '@/api/reportApi';
import { useToastStore } from '@/stores/toastStore';
import { formatShortAddress } from '@/utils/formatters';
const props = defineProps({
  building: Object,
  order: Number,
});

const emit = defineEmits(['toggle', 'unlike', 'diagnosed']);

const toastStore = useToastStore();
const isDiagnosing = ref(false);

const isDiagnosed = computed(() => props.building.safetyScore !== null);

function gradeMeta(grade) {
  if (!grade) return null;
  const key = GRADE_META[grade] ? grade : getGradeByStatusCode(grade);
  return GRADE_META[key];
}

function handleClick() {
  if (!isDiagnosed.value) return;
  emit('toggle');
}

async function handleDiagnose() {
  isDiagnosing.value = true;
  try {
    const report = await fetchReportData(props.building.buildingId);
    emit('diagnosed', {
      buildingId: props.building.buildingId,
      safetyScore: report.score,
      safetyGrade: report.grade,
    });
  } catch (error) {
    console.error('진단 조회 실패:', error);
    toastStore.showToast('진단에 실패했어요. 잠시 후 다시 시도해주세요.');
  } finally {
    isDiagnosing.value = false;
  }
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
      <span v-else-if="isDiagnosing" class="flex items-center gap-1 text-[13px] text-text-sub">
        <Loader2 :size="14" class="animate-spin" />
        진단 중
      </span>
      <button
        v-else
        @click.stop="handleDiagnose"
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
