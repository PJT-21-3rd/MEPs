<script setup>
//  #138 - AI 핵심 브리핑 컴포넌트 구현

defineProps({
  loading: {
    type: Boolean,
    default: true,
  },
  briefing: {
    type: String,
    default: '',
  },
});
</script>

<template>
  <div class="bg-surface-blue rounded-xl p-4">
    <p class="text-xs font-semibold text-button-primary mb-2">AI 핵심 브리핑</p>

    <!-- 로딩 스켈레톤 -->
    <div v-if="loading" class="flex flex-col gap-2">
      <div class="skeleton-shimmer h-3.5 rounded w-full" />
      <div class="skeleton-shimmer h-3.5 rounded w-4/5" />
    </div>

    <!-- 브리핑 텍스트 -->
    <p v-else class="text-sm text-text-main leading-relaxed">
      {{ briefing }}
    </p>
  </div>
</template>

<style scoped>
.skeleton-shimmer {
  background: linear-gradient(
    90deg,
    var(--color-surface-gray, #f5f5f5) 25%,
    #ececec 37%,
    var(--color-surface-gray, #f5f5f5) 63%
  );
  background-size: 400% 100%;
  animation: shimmer 1.4s ease-in-out infinite;
}

@keyframes shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}
</style>
