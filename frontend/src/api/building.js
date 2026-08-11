// 건물 리스트 api
import api from '@/api/index';

// 현재 지도 영역 내 건물리스트 조회
export const fetchNearbyBuildings = async (swLat, swLng, neLat, neLng, zoom) => {
  try {
    console.log(`백엔드 건물리스트 DB 검색 요청 중... (/api/buildings/nearby)`);
    const response = await api.get('/api/buildings/nearby', {
      params: {
        swLat,
        swLng,
        neLat,
        neLng,
        zoom,
      },
    });
    return response.data;
  } catch (error) {
    console.error('건물 리스트 API 호출 실패:', error);
    throw error;
  }
};

// 리스트 기반 건물 상세 정보(토지 + 건축물 + 폴리곤)
export const fetchBuildingDetail = async (buildingId) => {
  try {
    console.log(`백엔드 건물 상세 DB 검색 요청 중... (/api/buildings/${buildingId})`);
    const response = await api.get(`/api/buildings/${buildingId}`);
    return response.data;
  } catch (error) {
    console.error(`건물 상세 정보 호출 실패 (ID: ${buildingId}):`, error);
    throw error;
  }
};

// 좌표 기반 건물 상세 정보
export const fetchBuildingDetailByCoord = async (lat, lng) => {
  try {
    console.log(`(lat: ${lat}, lng: ${lng}) 좌표로 건물 정보 호출`);
    const response = await api.get('/api/buildings/point', {
      params: { lat, lng },
    });
    return response.data;
  } catch (error) {
    console.error(`좌표 기반 건물 정보 호출 실패 (lat: ${lat}, lng: ${lng}):`, error);
    throw error;
  }
};
