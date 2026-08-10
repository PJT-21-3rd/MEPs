// 지도/공간정보 관련 API (Reverse Geocoding)
import axios from 'axios';

export const fetchReverseGeocoding = async (lat, lng) => {
  try {
    const response = await axios.get('/maps-api/map-reversegeocode/v2/gc', {
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
