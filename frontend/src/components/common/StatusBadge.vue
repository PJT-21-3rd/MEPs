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
  size: {
    type: String,
    default: 'md',
    validator: (v) => ['sm', 'md'].includes(v),
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

const sizeClass = computed(() =>
  props.size === 'sm' ? 'text-xs px-2 py-0.5 gap-1' : 'text-sm px-2.5 py-1 gap-1.5',
);

const iconSizeClass = computed(() => (props.size === 'sm' ? 'w-3.5 h-3.5' : 'w-4 h-4'));
</script>

<template>
  <span
    class="inline-flex items-center gap-1 rounded-full font-semibold"
    :class="[meta.badgeBg, meta.text, sizeClass]"
  >
    <component :is="icon" :class="[iconSizeClass, meta.text]" stroke-width="2" aria-hidden="true" />
    {{ meta.label }}
  </span>
</template>
