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
    console.log(`현재 위치 행정동 코드 확인: ${hjdCode} (${admResult.region.area3.name})`);

    return admResult?.code?.id || null;
  } catch (error) {
    console.error('Reverse Geocoding API Error:', error);
    throw error;
  }
};
