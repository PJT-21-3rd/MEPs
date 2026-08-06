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
        class="flex-1 min-w-0 p-3 bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm"
      >
        <div class="flex items-center gap-4">
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
