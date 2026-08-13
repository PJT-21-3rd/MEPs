// 행정동/구별 메인화면 브리핑 관련 api
import api from '@/api/index';

// 행정동 AI 브리핑 데이터 호출
export const fetchHjdBriefing = async (hjdCode) => {
  try {
    const response = await api.get(`/api/hjd/${hjdCode}/briefing`);
    return response.data;
  } catch (error) {
    console.error(`동 단위 브리핑 API Error (hjdCode: ${hjdCode}):`, error);
    throw error;
  }
};

// 구 단위 (SGG) 브리핑
export const fetchSggBriefing = async (sggCode) => {
  try {
    const response = await api.get(`/api/sgg/${sggCode}/briefing`);
    return response.data;
  } catch (error) {
    console.error('구 단위 브리핑 API 호출 실패:', error);
    throw error;
  }
};
