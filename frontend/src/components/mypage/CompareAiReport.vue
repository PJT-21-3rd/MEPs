<script setup>
import ScoreGauge from '@/components/report/ScoreGauge.vue';
import AiBriefingCard from '@/components/report/AiBriefingCard.vue';
import DiagnosticFactorList from '@/components/report/DiagnosticFactorList.vue';
import { Sparkles } from '@lucide/vue';
import DetailedReportDisclaimer from '../report/DetailedReportDisclaimer.vue';
const props = defineProps({
  buildings: Array,
  activeSections: Array,
});

function isActive(key) {
  return props.activeSections.includes(key);
}

function gradeClass(grade) {
  if (grade === '안전') return 'bg-grade-safe text-text-safe';
  if (grade === '양호') return 'bg-grade-good text-text-good';
  if (grade === '주의') return 'bg-grade-warn text-text-warn';
  return '';
}
</script>

<template>
  <div v-if="isActive('report')" class="mt-4">
    <h4 class="flex items-center gap-1.5 text-[17px] font-bold text-primary mb-1">
      <Sparkles :size="17" />
      AI 안전진단
    </h4>
    <div class="flex gap-4">
      <div
        v-for="building in buildings"
        :key="building.buildingId"
        class="flex-1 min-w-0 p-4 bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm"
      >
        <!-- 매물 헤더 -->
        <div class="-mx-4 -mt-4 px-3 py-2.5 bg-surface-gray flex items-center justify-between">
          <!-- 왼쪽: 아이콘+건물명-->
          <div class="flex items-center gap-1.5">
            <Sparkles :size="15" class="text-secondary" />
            <span class="text-[14px] font-semibold">{{ building.bldNm }}</span>
          </div>

          <!-- 오른쪽: 등급 배지 -->
          <span class="text-[12px] px-2 py-0.5 rounded-full" :class="gradeClass(building.grade)">
            {{ building.grade }}
          </span>
        </div>

        <!-- 게이지+브리핑 -->
        <div class="flex items-center gap-4 py-2">
          <div class="scale-[0.7] origin-center shrink-0 -mx-6 -my-6">
            <ScoreGauge :score="building.score" :showLabel="false" />
          </div>
          <div class="flex-1 min-w-0">
            <AiBriefingCard :loading="false" :briefing="building.briefing" />
          </div>
        </div>
        <DiagnosticFactorList :items="building.diagnosis" mode="summary" class="mt-3" />
        <div class="mt-3">
          <DetailedReportDisclaimer />
        </div>
      </div>
    </div>
  </div>
</template>
