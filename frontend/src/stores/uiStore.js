// src/stores/uiStore.js
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { fetchBuildingDetail, fetchBuildingDetailByCoord } from '@/api/building';

export const useUiStore = defineStore('ui', () => {
  const searchQuery = ref(''); // 검색어
  const selectedBuildingId = ref(null); // 건물선택
  const isDetailOpen = ref(false); // 상세화면 열람
  const isReportOpen = ref(false); // 리포트화면 열람
  const isRoadViewModalOpen = ref(false); // 로드뷰
  const roadViewCoords = ref({ lat: null, lng: null });
  const insuranceModalConfig = ref(null); // 보험 모달 설정
  const loanModalConfig = ref(null); // 대출 모달 설정
  const currentBriefing = ref(null); // 브리핑 데이터
  const briefingLevel = ref('dong');
  const currentSort = ref('POPULAR'); // 정렬
  const currentBuildings = ref([]); // 현재 화면의 건물 배열
  const currentBuildingDetail = ref(null); // 현재 건물 상세 데이터
  const isDetailLoading = ref(false);
  const preventMapMove = ref(false);
  const isZoomRequired = ref(false);
  const recentBuildings = ref([]); // 최근 본 매물
  const RECENT_KEY = 'meps_recent_buildings';

  const toggleDetailPanel = () => {
    isDetailOpen.value = !isDetailOpen.value;
  };
  // 로컬스토리지에서 데이터 불러오기
  const loadRecentBuildings = () => {
    const stored = localStorage.getItem(RECENT_KEY);
    if (stored) {
      try {
        recentBuildings.value = JSON.parse(stored);
      } catch (e) {
        console.error('로컬스토리지 파싱 에러:', e);
        recentBuildings.value = [];
      }
    }
  };
  const addRecentBuilding = (buildingData) => {
    console.log(buildingData);
    if (!buildingData || !buildingData.buildingId) return;

    const summaryBuilding = {
      buildingId: buildingData.buildingId,
      bldNm: buildingData.bldNm,
      jibunAddr: buildingData.jibunAddr,
      roadAddr: buildingData.roadAddr,
      mainPurpsNm: buildingData.mainPurpsNm,
      archArea: buildingData.archArea,
      grndFlr: buildingData.detail?.grndFlr,
      ugrndFlr: buildingData.detail?.ugrndFlr,
      useAprDay: buildingData.detail?.useAprDay,
      lat: buildingData.lat,
      lng: buildingData.lng,
    };
    console.log(summaryBuilding);

    let list = [...recentBuildings.value];
    list = list.filter((b) => b.buildingId !== summaryBuilding.buildingId); // 중복 제거
    list.unshift(summaryBuilding);

    if (list.length > 20) {
      list = list.slice(0, 20);
    }

    recentBuildings.value = list;
    localStorage.setItem(RECENT_KEY, JSON.stringify(list));
  };
  loadRecentBuildings();

  // 상세화면 여닫
  const openBuildingDetail = async (buildingId) => {
    preventMapMove.value = false;

    selectedBuildingId.value = buildingId;
    isDetailOpen.value = true;
    isDetailLoading.value = true;
    currentBuildingDetail.value = null;

    try {
      const data = await fetchBuildingDetail(buildingId);
      currentBuildingDetail.value = data;
      addRecentBuilding(data);
    } catch (error) {
      currentBuildingDetail.value = null;
    } finally {
      isDetailLoading.value = false;
    }
  };
  const openBuildingDetailByCoord = async (lat, lng) => {
    preventMapMove.value = true;

    isDetailOpen.value = true;
    isDetailLoading.value = true;
    currentBuildingDetail.value = null;

    try {
      const data = await fetchBuildingDetailByCoord(lat, lng);
      if (data && data.buildingId) {
        selectedBuildingId.value = data.buildingId;
        currentBuildingDetail.value = data;
        addRecentBuilding(data);
        return data.buildingId;
      } else {
        closeBuildingDetail(); // 길거리나 빈 땅
        return null;
      }
    } catch (error) {
      closeBuildingDetail();
      console.log('클릭한 위치에 건물 정보가 없습니다.');
      return null;
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

  const setSort = (sortType) => {
    currentSort.value = sortType;
  };

  const setBriefingData = (data, level) => {
    currentBriefing.value = data;
    briefingLevel.value = level;
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
    currentBriefing,
    briefingLevel,
    loanModalConfig,
    currentSort,
    currentBuildings,
    currentBuildingDetail,
    isDetailLoading,
    preventMapMove,
    isZoomRequired,
    recentBuildings,
    addRecentBuilding,
    openBuildingDetail,
    openBuildingDetailByCoord,
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
    setSort,
    setBriefingData,
    setBuildingsData,
  };
});
