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
  </Story>
</template>

<script setup>
import CommercialAiBriefing from './CommercialAiBriefing.vue';
</script>
