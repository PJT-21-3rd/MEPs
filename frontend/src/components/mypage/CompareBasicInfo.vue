<script setup>
import RoadViewImage from '@/components/detail/RoadViewImage.vue';
import BuildingInfoPannel from '@/components/detail/BuildingInfoPannel.vue';

defineProps({
  buildings: Array,
});
</script>

<template>
  <div class="flex gap-4">
    <div
      v-for="building in buildings"
      :key="building.buildingId"
      class="flex-1 min-w-0 bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm"
    >
      <RoadViewImage :lat="building.lat" :lng="building.lng" />
      <div class="p-4">
        <BuildingInfoPannel :buildingData="building" />
      </div>
    </div>
  </div>
</template>
