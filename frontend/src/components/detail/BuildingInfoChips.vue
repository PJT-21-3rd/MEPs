<script setup>
import { computed } from 'vue';
import { Heart } from '@lucide/vue';
import { formatArea } from '@/utils/formatters';
import InfoChip from './InfoChip.vue';

const props = defineProps({
  buildingData: { type: Object, default: () => ({}) },
});

const platAreaStr = computed(() => formatArea(props.buildingData?.platArea));
const totAreaStr = computed(() => formatArea(props.buildingData?.totArea));
</script>

<template>
  <div class="mt-4">
    <div class="flex gap-2">
      <InfoChip label="대지면적" :value="platAreaStr" />
      <InfoChip label="연면적" :value="totAreaStr" />
    </div>

    <div class="flex items-center justify-end">
      <span class="text-[12px] text-text-sub flex items-center gap-1">
        <Heart :size="12" class="fill-status-like text-status-like" />
        {{ buildingData.savedCnt }} 명이 저장함
      </span>
    </div>
  </div>
</template>
