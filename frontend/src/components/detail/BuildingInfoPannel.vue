<script setup>
import { computed } from 'vue';

const props = defineProps({
  buildingData: { type: Object, default: () => null },
});

const hasBuildingName = computed(() => {
  return !!(props.buildingData?.bldNm && props.buildingData.bldNm.trim() !== '');
});
</script>

<template>
  <div v-if="buildingData">
    <h2
      v-if="hasBuildingName"
      class="mt-1 text-[20px] leading-tight tracking-tight text-neutral-900"
    >
      {{ buildingData.bldNm }}
    </h2>
    <div class="mt-1.5 space-y-1">
      <div class="flex items-center gap-1.5">
        <span class="rounded bg-neutral-100 px-1.5 py-0.5 text-[12px] text-neutral-500">
          지번
        </span>
        <p class="text-neutral-700" :class="hasBuildingName ? 'text-[15px]' : 'text-[20px] '">
          {{ buildingData.jibunAddr }}
        </p>
      </div>
      <div class="flex items-center gap-1.5">
        <span class="rounded bg-neutral-100 px-1.5 py-0.5 text-[12px] text-neutral-400">
          도로명
        </span>
        <p class="text-neutral-400" :class="hasBuildingName ? 'text-[14px]' : 'text-[15px]'">
          {{ buildingData.roadAddr }}
        </p>
      </div>
    </div>
  </div>
</template>
