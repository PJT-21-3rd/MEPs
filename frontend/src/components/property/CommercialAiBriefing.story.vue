<!-- src/components/property/CommercialAiBriefing.story.vue -->
<template>
  <Story title="CommercialAiBriefing" group="property">
    <Variant title="기본 (Default)">
      <!-- 실제 사이드바 너비(400px)와 패딩을 시뮬레이션 -->
      <div class="w-[400px] bg-white border border-neutral-200 h-screen">
        <div class="px-6 pb-5 pt-5">
          <CommercialAiBriefing />
        </div>
      </div>
    </Variant>

    <Variant title="Loading State">
      <div class="w-[400px] h-screen bg-white pt-4 shadow-lg">
        <div class="px-6 mb-2">
          <div class="rounded-t-2xl bg-surface-blue px-5 pb-3 pt-4">
            <div class="flex items-center gap-2">
              <span
                class="flex items-center gap-1 rounded-full bg-button-primary px-2.5 py-1 text-[12px] text-white"
              >
                <Sparkles :size="12" /> AI 브리핑
              </span>
              <div class="h-6 w-32 rounded bg-surface-gray animate-pulse" />
            </div>
          </div>
        </div>

        <CommercialAiBriefingSkeleton />
      </div>
    </Variant>
  </Story>
</template>

<script setup>
import { Sparkles } from '@lucide/vue';
import CommercialAiBriefing from './CommercialAiBriefing.vue';
import CommercialAiBriefingSkeleton from './CommercialAiBriefingSkeleton.vue';
</script>
