<script setup>
import { computed } from 'vue';
import { Heart } from '@lucide/vue';
import InfoChip from './InfoChip.vue';

const props = defineProps({
  buildingData: { type: Object, default: () => ({}) },
});

const platArea = computed(() =>
  props.buildingData?.platArea ? `${props.buildingData.platArea} ㎡` : '-',
);
const totArea = computed(() =>
  props.buildingData?.totArea ? `${props.buildingData.totArea} ㎡` : '-',
);
</script>

<template>
  <div class="flex gap-2">
    <InfoChip label="대지면적" :value="platArea" />
    <InfoChip label="연면적" :value="totArea" />
  </div>

  <div class="flex items-center justify-end p-1">
    <span class="text-[12px] text-neutral-500 flex items-center gap-1">
      <Heart :size="12" class="fill-status-like text-status-like" /> 0 명이 저장함
    </span>
  </div>
</template>
