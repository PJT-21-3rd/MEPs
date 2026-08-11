// src/stores/uiStore.js
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { fetchBuildingDetail } from '@/api/building';

export const useUiStore = defineStore('ui', () => {
  const searchQuery = ref(''); // 검색어
  const selectedBuildingId = ref(null); // 건물선택
  const isDetailOpen = ref(false); // 상세화면 열람
  const isReportOpen = ref(false); // 리포트화면 열람
  const isRoadViewModalOpen = ref(false); // 로드뷰
  const roadViewCoords = ref({ lat: null, lng: null });
  const insuranceModalConfig = ref(null); // 보험 모달 설정
  const loanModalConfig = ref(null); // 대출 모달 설정
  const hjdBriefingData = ref(null); // 행정동 브리핑 데이터
  const currentBuildings = ref([]); // 현재 화면의 건물 배열
  const currentBuildingDetail = ref(null); // 현재 건물 상세 데이터
  const isDetailLoading = ref(false);
  const isZoomRequired = ref(false);

  const toggleDetailPanel = () => {
    isDetailOpen.value = !isDetailOpen.value;
  };

  // 상세화면 여닫
  const openBuildingDetail = async (buildingId) => {
    selectedBuildingId.value = buildingId;
    isDetailOpen.value = true;
    isDetailLoading.value = true;

    try {
      const data = await fetchBuildingDetail(buildingId);
      currentBuildingDetail.value = data;
    } catch (error) {
      currentBuildingDetail.value = null;
    } finally {
      isDetailLoading.value = false;
    }
  };
  const closeBuildingDetail = () => {
    isDetailOpen.value = false;
    selectedBuildingId.value = null;
    isReportOpen.value = false;
    currentBuildingDetail.value = null;
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

  // 보험 모달 여닫
  const openInsuranceModal = (config) => {
    insuranceModalConfig.value = config;
  };
  const closeInsuranceModal = () => {
    insuranceModalConfig.value = null;
  };

  // 대출 모달 여닫
  const openLoanModal = (config) => {
    loanModalConfig.value = config;
  };
  const closeLoanModal = () => {
    loanModalConfig.value = null;
  };

  const setHjdBriefingData = (data) => {
    hjdBriefingData.value = data;
  };

  const setBuildingsData = (data) => {
    isZoomRequired.value = data.zoomRequired;
    currentBuildings.value = data.buildings || [];
  };

  return {
    isDetailOpen,
    isReportOpen,
    isRoadViewModalOpen,
    searchQuery,
    selectedBuildingId,
    roadViewCoords,
    insuranceModalConfig,
    hjdBriefingData,
    loanModalConfig,
    currentBuildings,
    currentBuildingDetail,
    isDetailLoading,
    isZoomRequired,
    openBuildingDetail,
    closeBuildingDetail,
    openReport,
    closeReport,
    openRoadViewModal,
    closeRoadViewModal,
    toggleDetailPanel,
    openInsuranceModal,
    closeInsuranceModal,
    openLoanModal,
    closeLoanModal,
    setHjdBriefingData,
    setBuildingsData,
  };
});
