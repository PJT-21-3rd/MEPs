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
  typingStage: {
    type: Number,
    default: 5,
  }, //타이핑 중 (0-5)
});

defineEmits(['typing-done']);
</script>

<template>
  <div class="flex flex-col" :class="mode === 'detail' ? 'gap-2' : 'gap-3'">
    <p v-if="mode === 'summary'" class="text-base font-bold text-text-main">진단 근거 데이터</p>

    <StructureStabilityItem
      v-bind="items.structure"
      :mode="mode"
      :typing-active="typingStage === 1"
      :already-typed="typingStage > 1"
      @typing-done="$emit('typing-done')"
    />
    <FireSafetyItem
      v-bind="items.fire"
      :mode="mode"
      :typing-active="typingStage === 2"
      :already-typed="typingStage > 2"
      @typing-done="$emit('typing-done')"
    />
    <SinkholeHistoryItem
      v-bind="items.sinkhole"
      :mode="mode"
      :typing-active="typingStage === 3"
      :already-typed="typingStage > 3"
      @typing-done="$emit('typing-done')"
    />
    <FloodHistoryItem
      v-bind="items.flood"
      :mode="mode"
      :typing-active="typingStage === 4"
      :already-typed="typingStage > 4"
      @typing-done="$emit('typing-done')"
    />
  </div>
</template>
