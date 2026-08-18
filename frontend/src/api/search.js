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

// tag제거
const cleanHtmlTitle = (title) => {
  return title
    .replace(/<[^>]+>/g, '')
    .replace(/&quot;/g, '"')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&apos;/g, "'");
};
// 네이버 ncloud 뉴스 검색 api
export const fetchLocalRealEstateNews = async (sggName, hjdName, sidoName = '서울') => {
  try {
    const location = `${sggName || ''} ${hjdName || ''}`.trim();
    const query = `${sidoName} ${location} 부동산`;

    const response = await axios.get('/api-hub/search/v1/news', {
      params: {
        query: query,
        display: 5,
        sort: 'sim',
      },
      headers: {
        'X-NCP-APIGW-API-KEY-ID': import.meta.env.VITE_NCLOUD_CLIENT_ID,
        'X-NCP-APIGW-API-KEY': import.meta.env.VITE_NCLOUD_CLIENT_SECRET,
      },
    });

    console.log(response.data);

    if (response.data.items && response.data.items.length > 0) {
      return response.data.items.map((item) => ({
        title: cleanHtmlTitle(item.title),
        link: item.link,
      }));
    }

    return [{ title: `'${location}' 관련 최신 부동산 뉴스가 없습니다.`, link: '#' }];
  } catch (error) {
    console.error('네이버 뉴스 로드 API 에러:', error);
    return [{ title: '뉴스를 불러오는 데 실패했습니다.', link: '#' }];
  }
};
