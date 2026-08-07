// src/stores/uiStore.js
import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUiStore = defineStore('ui', () => {
  const searchQuery = ref(''); // 검색어
  const selectedBuildingId = ref(null); // 건물선택
  const isDetailOpen = ref(false); // 상세화면 열람
  const isReportOpen = ref(false); // 리포트화면 열람
  const isRoadViewModalOpen = ref(false); // 로드뷰
  const roadViewCoords = ref({ lat: null, lng: null });

  const toggleDetailPanel = () => {
    isDetailOpen.value = !isDetailOpen.value;
  };

  // 상세화면 여닫
  const openBuildingDetail = (buildingId) => {
    selectedBuildingId.value = buildingId;
    isDetailOpen.value = true;
  };
  const closeBuildingDetail = () => {
    isDetailOpen.value = false;
    selectedBuildingId.value = null;
    isReportOpen.value = false;
  };

  // 리포트화면 여닫
  const openReport = () => {
    isReportOpen.value = true;
  };
  const closeReport = () => {
    isReportOpen.value = false;
  };

  // 로드뷰 여닫
  const openRoadViewModal = (lat, lng) => {
    roadViewCoords.value = { lat, lng };
    isRoadViewModalOpen.value = true;
  };
  const closeRoadViewModal = () => {
    isRoadViewModalOpen.value = false;
  };

  return {
    isDetailOpen,
    isReportOpen,
    isRoadViewModalOpen,
    searchQuery,
    selectedBuildingId,
    roadViewCoords,
    openBuildingDetail,
    closeBuildingDetail,
    openReport,
    closeReport,
    openRoadViewModal,
    closeRoadViewModal,
    toggleDetailPanel,
  };
});
