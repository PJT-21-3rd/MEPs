// 행정동/구별 메인화면 브리핑 관련 api
import api from '@/api/index';

// 행정동 AI 브리핑 데이터 호출
export const fetchHjdBriefing = async (hjdCode) => {
  try {
    const response = await api.get(`/api/hjd/${hjdCode}/briefing`);
    return response.data;
  } catch (error) {
    console.error(`hjd Briefing API Error (hjdCode: ${hjdCode}):`, error);
    throw error;
  }
};
