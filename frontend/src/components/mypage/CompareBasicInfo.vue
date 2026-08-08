<script setup>
import { useRouter } from 'vue-router';
import RoadViewImage from '@/components/detail/RoadViewImage.vue';
import BuildingInfoPannel from '@/components/detail/BuildingInfoPannel.vue';

defineProps({
  buildings: Array,
});

const router = useRouter();

function goToDetail(buildingId) {
  router.push({ path: '/', query: { buildingId } });
}
</script>

<template>
  <div class="flex gap-4">
    <div
      v-for="building in buildings"
      :key="building.buildingId"
      @click="goToDetail(building.buildingId)"
      class="flex-1 min-w-0 bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm cursor-pointer hover:shadow-md transition-shadow"
    >
      <RoadViewImage :lat="building.lat" :lng="building.lng" />
      <div class="p-4">
        <BuildingInfoPannel :buildingData="building" />
      </div>
    </div>
  </div>
</template>
