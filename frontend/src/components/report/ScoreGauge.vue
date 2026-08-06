<script setup>
import { computed, ref, onMounted, watch } from 'vue';
import { GRADE_META, getGradeByScore } from '@/constants/reportConstants';

// #24, #30

const props = defineProps({
  score: {
    type: Number,
    required: true,
    validator: (v) => v >= 0 && v <= 100,
  },
  grade: {
    type: String,
    default: null,
    validator: (v) => v === null || ['safe', 'good', 'warning', 'danger'].includes(v),
  },
  animate: {
    type: Boolean,
    default: true,
  },
  showLabel: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['animation-end']);

const grade = computed(() => props.grade ?? getGradeByScore(props.score));
const gradeMeta = computed(() => GRADE_META[grade.value]);

// 원형 게이지 계산
const RADIUS = 54;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

const displayScore = ref(props.animate ? 0 : props.score);

const dashOffset = computed(() => {
  const ratio = Math.min(Math.max(displayScore.value, 0), 100) / 100;
  return CIRCUMFERENCE * (1 - ratio);
});

function animateScore() {
  const start = 0;
  const end = props.score;
  const duration = 800;
  const startTime = performance.now();

  function step(now) {
    const elapsed = now - startTime;
    const progress = Math.min(elapsed / duration, 1);
    // ease-out
    const eased = 1 - Math.pow(1 - progress, 3);
    displayScore.value = Math.round(start + (end - start) * eased);

    if (progress < 1) {
      requestAnimationFrame(step);
    } else {
      displayScore.value = end;
      emit('animation-end');
    }
  }

  requestAnimationFrame(step);
}

onMounted(() => {
  if (props.animate) {
    animateScore();
  } else {
    emit('animation-end');
  }
});

watch(
  () => props.score,
  () => {
    if (props.animate) {
      animateScore();
    } else {
      displayScore.value = props.score;
    }
  },
);
</script>

<template>
  <div class="flex flex-col items-center gap-3 py-6">
    <p v-if="showLabel" class="text-sm text-text-sub">종합 AI 안심 스코어</p>

    <div class="relative w-40 h-40">
      <svg viewBox="0 0 120 120" class="w-full h-full -rotate-90">
        <circle
          class="fill-none stroke-surface-gray"
          stroke-width="10"
          cx="60"
          cy="60"
          :r="RADIUS"
        />
        <circle
          class="fill-none transition-colors duration-300"
          :class="gradeMeta.stroke"
          stroke-width="10"
          stroke-linecap="round"
          cx="60"
          cy="60"
          :r="RADIUS"
          :stroke-dasharray="CIRCUMFERENCE"
          :stroke-dashoffset="dashOffset"
        />
      </svg>

      <div class="absolute inset-0 flex flex-col items-center justify-center">
        <span class="text-4xl font-bold leading-none" :class="gradeMeta.text">
          {{ displayScore }}
        </span>
        <span class="text-sm text-text-sub mt-1">/ 100</span>
      </div>
    </div>

    <span
      v-if="showLabel"
      class="px-4 py-1.5 rounded-full text-sm font-semibold"
      :class="[gradeMeta.badgeBg, gradeMeta.text]"
    >
      {{ gradeMeta.label }} 등급
    </span>
  </div>
</template>
