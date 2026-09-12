<script setup>
import { computed } from 'vue';
import { GRADE_META } from '@/constants/reportConstants';
import { ShieldCheck, Check, AlertTriangle, OctagonAlert } from '@lucide/vue';

// #25 - 진단 근거 판정배지 (안전, 양호, 주의, 위험)

const props = defineProps({
  status: {
    type: String,
    required: true,
    validator: (v) => ['safe', 'good', 'warning', 'danger'].includes(v),
  },
});

const ICON_MAP = {
  safe: ShieldCheck,
  good: Check,
  warning: AlertTriangle,
  danger: OctagonAlert,
};
const meta = computed(() => GRADE_META[props.status]);
const icon = computed(() => ICON_MAP[props.status]);
</script>

<template>
  <span
    class="inline-flex items-center gap-1 rounded-full font-semibold text-xs px-[10px] py-1"
    :class="[meta.badgeBg, meta.text]"
  >
    <component
      :is="icon"
      class="w-3.5 h-3.5"
      :class="meta.text"
      stroke-width="2"
      aria-hidden="true"
    />
    {{ meta.label }}
  </span>
</template>
