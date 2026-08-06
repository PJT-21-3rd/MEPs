<script setup>
import { computed } from 'vue';
import StatusBadge from '@/components/common/StatusBadge.vue';
import { Flame, Info } from '@lucide/vue';

const props = defineProps({
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
  aiReport: {
    type: String,
    default: '',
  },
});

const aiReportParagraphs = computed(() => {
  if (!props.aiReport) return [];
  return props.aiReport.split(/\n\s*\n/).filter((p) => p.trim());
});
</script>

<template>
  <div
    class="p-4 rounded-xl flex flex-col gap-3"
    :class="mode === 'detail' ? '' : 'border border-surface-gray'"
  >
    <!-- summary 뷰 -->
    <template v-if="mode === 'summary'">
      <div class="flex items-center gap-2">
        <Flame class="w-4 h-4 text-text-sub" />
        <span class="flex-1 text-sm font-semibold text-text-main">화재안정성</span>
        <StatusBadge :status="status" />
      </div>
      <p class="text-sm text-text-secondary">"{{ summary }}"</p>
    </template>

    <!-- detail 뷰 -->
    <template v-else>
      <div class="flex items-center gap-2">
        <span
          class="w-7 h-7 shrink-0 rounded-[10px] bg-surface-blue flex items-center justify-center"
        >
          <Flame class="w-4 h-4 text-button-primary" />
        </span>
        <span class="flex-1 text-sm font-semibold text-text-main">2. 화재안정성</span>
        <StatusBadge :status="status" />
      </div>

      <p class="text-xs text-text-sub">화재 위험도 및 소방 접근성 분석</p>

      <div
        v-if="aiReportParagraphs.length"
        class="bg-surface-sky rounded-[16px] p-3 flex flex-col gap-1.5"
      >
        <div class="flex items-center gap-1.5">
          <Info class="w-3.5 h-3.5 text-button-primary shrink-0" />
          <span class="text-[13px] font-Regular text-button-primary">AI 전문가 의견</span>
        </div>
        <p
          v-for="(paragraph, idx) in aiReportParagraphs"
          :key="idx"
          class="text-[14px] font-Regular text-text-detail"
        >
          {{ paragraph }}
        </p>
      </div>
    </template>

    <!-- 특약 카드: 주의 등급이면 summary/detail 상관없이 항상 노출 -->
    <div v-if="status === 'warning' && detail?.insurance">
      <div class="bg-surface-base rounded-lg p-3">
        <p class="text-sm font-semibold text-text-main mb-0.5">{{ detail.insurance.name }}</p>
        <p class="text-xs text-text-sub">{{ detail.insurance.description }}</p>
      </div>
    </div>
  </div>
</template>
