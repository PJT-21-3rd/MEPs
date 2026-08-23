<script setup>
import { computed } from 'vue';

const props = defineProps({
  buildingData: { type: Object, default: () => null },
  isStuck: { type: Boolean, default: false },
});

const hasBuildingName = computed(() => {
  return !!(props.buildingData?.bldNm && props.buildingData.bldNm.trim() !== '');
});
</script>

<template>
  <div
    v-if="buildingData"
    class="px-5 sticky top-0 z-20 bg-white transition-all duration-200"
    :class="isStuck ? 'pt-0' : 'pt-4'"
  >
    <h2 v-if="hasBuildingName" class="mt-1 text-[20px] leading-tight tracking-tight text-text-main">
      {{ buildingData.bldNm }}
    </h2>
    <div class="mt-1.5 space-y-1">
      <div class="flex items-center gap-1.5">
        <span
          v-show="!isStuck"
          class="shrink-0 rounded bg-surface-gray px-1.5 py-0.5 text-[12px] text-text-secondary"
        >
          지번
        </span>
        <p class="text-text-secondary" :class="hasBuildingName ? 'text-[15px]' : 'text-[20px] '">
          {{ buildingData.jibunAddr }}
        </p>
      </div>
      <div v-show="!isStuck || !hasBuildingName" class="flex items-center gap-1.5">
        <span
          v-show="!isStuck || !hasBuildingName"
          class="shrink-0 rounded bg-surface-gray px-1.5 py-0.5 text-[12px] text-text-sub"
        >
          도로명
        </span>
        <p class="text-text-sub" :class="hasBuildingName ? 'text-[14px]' : 'text-[15px]'">
          {{ buildingData.roadAddr }}
        </p>
      </div>
    </div>
  </div>
</template>
