// 지도/공간정보 관련 API (Reverse Geocoding)
import api from '@/api/index';

// 행정동 코드 reverse geocode
export const fetchReverseGeocoding = async (lat, lng) => {
  try {
    const response = await api.get('/maps-api/map-reversegeocode/v2/gc', {
      params: {
        coords: `${lng},${lat}`,
        orders: 'admcode',
        output: 'json',
      },
      headers: {
        'X-NCP-APIGW-API-KEY-ID': import.meta.env.VITE_NAVER_MAP_CLIENT_ID,
        'X-NCP-APIGW-API-KEY': import.meta.env.VITE_NAVER_MAP_CLIENT_SECRET,
      },
    });

    const results = response.data.results;
    if (!results || results.length === 0) return null;

    const admResult = results.find((r) => r.name === 'admcode');
    if (!admResult || !admResult.code) return null;

    const originalCode = admResult.code.id;
    const hjdCode8Digits = originalCode.substring(0, 8);
    console.log(`[코드 파싱] 원본 10자리: ${originalCode} ➔ 변환된 8자리: ${hjdCode8Digits}`);

    return hjdCode8Digits;
  } catch (error) {
    console.error('Reverse Geocoding API Error:', error);
    throw error;
  }
};

/**
 * 행정동(시군구, 동) 리스트 조회 API
 * @returns {Promise<Array>} 행정동 리스트 데이터
 */
export const fetchRegionList = async () => {
  try {
    const response = await api.get('/api/hjd');
    console.log('📌 API 원본 응답:', response.data);
    return response.data; // 응답 데이터 반환
  } catch (error) {
    console.error('행정동 데이터를 불러오는 API 호출 실패:', error);
    throw error;
  }
};
