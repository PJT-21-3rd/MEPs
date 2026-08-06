<script setup>
import RoadViewImage from '@/components/detail/RoadViewImage.vue';
import BuildingInfoPannel from '@/components/detail/BuildingInfoPannel.vue';

defineProps({
  buildings: Array,
});
</script>

<template>
  <div class="flex gap-4 mb-6">
    <div v-for="building in buildings" :key="building.buildingId" class="flex-1 min-w-0">
      <div class="mb-3">
        <RoadViewImage :lat="building.lat" :lng="building.lng" />
      </div>
      <BuildingInfoPannel :buildingData="building" />
    </div>
  </div>
</template>
