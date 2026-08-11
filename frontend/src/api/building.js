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
