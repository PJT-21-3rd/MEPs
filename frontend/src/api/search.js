// 통합검색 관련 api
import api from '@/api/index';
import axios from 'axios';

// 백엔드 통합검색 api
export const fetchBackendSearch = async (keyword) => {
  try {
    console.log(`백엔드 자체 DB 검색 요청 중... (/api/buildings/search)`);
    const response = await api.get('/api/buildings/search', {
      params: { keyword },
    });
    return Array.isArray(response.data) ? response.data[0] : response.data;
  } catch (error) {
    console.error('Backend Search API Error:', error);
    throw error;
  }
};

// 네이버 ncloud 지역 검색 api
export const fetchNcloudLocalSearch = async (keyword) => {
  try {
    const response = await axios.get('/api-hub/search/v1/local', {
      params: {
        query: keyword,
        display: 1,
        format: 'json',
      },
      headers: {
        'X-NCP-APIGW-API-KEY-ID': import.meta.env.VITE_NCLOUD_CLIENT_ID,
        'X-NCP-APIGW-API-KEY': import.meta.env.VITE_NCLOUD_CLIENT_SECRET,
        Accept: 'application/json',
      },
    });

    const items = response.data.items;
    return items && items.length > 0 ? items[0] : null;
  } catch (error) {
    console.error('Ncloud Local Search API Error:', error);
    throw error;
  }
};
