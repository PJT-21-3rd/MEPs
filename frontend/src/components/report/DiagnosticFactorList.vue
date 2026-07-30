<script setup>
import StructureStabilityItem from './StructureStabilityItem.vue';
import FireSafetyItem from './FireSafetyItem.vue';
import SinkholeHistoryItem from './SinkholeHistoryItem.vue';
import FloodHistoryItem from './FloodHistoryItem.vue';

//  #25 - 4대 진단근거(구조/화재/지반침하/침수) 배치 컨테이너

defineProps({
  items: {
    type: Object,
    required: true,
  },
  mode: {
    type: String,
    default: 'summary',
    validator: (v) => ['summary', 'detail'].includes(v),
  },
});
</script>

<template>
  <div class="flex flex-col gap-3">
    <p class="text-base font-bold text-text-main">진단 근거 데이터</p>

    <StructureStabilityItem v-bind="items.structure" :mode="mode" />
    <FireSafetyItem v-bind="items.fire" :mode="mode" />
    <SinkholeHistoryItem v-bind="items.sinkhole" :mode="mode" />
    <FloodHistoryItem v-bind="items.flood" :mode="mode" />
  </div>
</template>
