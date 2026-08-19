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
      v-for="item in buildings"
      :key="item.building.buildingId"
      @click="goToDetail(item.building.buildingId)"
      class="flex-1 min-w-0 bg-white rounded-2xl border border-surface-gray overflow-hidden shadow-sm cursor-pointer hover:bg-surface-blue hover: transition-colors"
    >
      <RoadViewImage
        :lat="item.building.center?.coordinates?.[1]"
        :lng="item.building.center?.coordinates?.[0]"
      />
      <div class="p-4">
        <BuildingInfoPannel :buildingData="item.building" />
      </div>
    </div>
  </div>
</template>
