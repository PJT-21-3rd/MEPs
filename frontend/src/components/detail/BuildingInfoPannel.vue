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
    class="px-5 sticky top-0 z-20 bg-transparent transition-all duration-200"
    :class="isStuck ? 'bg-white py-2' : 'pt-4'"
  >
    <h2 v-if="hasBuildingName" class="mt-1 text-[20px] leading-tight tracking-tight text-text-main">
      {{ buildingData.bldNm }}
    </h2>
    <div class="mt-1.5 space-y-1">
      <div class="flex items-center">
        <span
          class="shrink-0 rounded bg-surface-gray py-0.5 text-[12px] text-text-secondary transition-all duration-300 ease-out overflow-hidden"
          :class="isStuck ? 'w-0 h-0 px-0 mr-0 opacity-0' : 'mr-1.5 px-1.5 opacity-100'"
        >
          지번
        </span>
        <p class="text-text-secondary" :class="hasBuildingName ? 'text-[15px]' : 'text-[20px] '">
          {{ buildingData.jibunAddr }}
        </p>
      </div>
      <div
        class="flex items-center transition-all duration-300 ease-out overflow-hidden"
        :class="!isStuck || !hasBuildingName ? 'max-h-[30px] opacity-100' : 'max-h-0 opacity-0'"
      >
        <span
          class="shrink-0 rounded bg-surface-gray py-0.5 text-[12px] text-text-sub transition-all duration-300 ease-out overflow-hidden"
          :class="isStuck ? 'w-0 h-0 px-0 mr-0 opacity-0' : 'mr-1.5 px-1.5 opacity-100'"
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
