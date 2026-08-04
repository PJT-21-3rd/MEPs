// src/stores/uiStore.js
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUiStore = defineStore('ui', () => {
  const isDetailOpen = ref(false);
  const searchQuery = ref('');
  const selectedBuildingId = ref(null);

  const toggleDetailPanel = () => {
    isDetailOpen.value = !isDetailOpen.value;
  };

  const openBuildingDetail = (buildingId) => {
    selectedBuildingId.value = buildingId;
    isDetailOpen.value = true;
  };
  const closeBuildingDetail = () => {
    isDetailOpen.value = false;
    selectedBuildingId.value = null;
  };

  return {
    isDetailOpen,
    searchQuery,
    selectedBuildingId,
    openBuildingDetail,
    closeBuildingDetail,
    toggleDetailPanel,
  };
});
