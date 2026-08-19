import http from '.';

// 찜하기
export async function addSaved(buildingId) {
  try {
    const { data } = await http.post(`/api/member/saved/${buildingId}`);
    return data;
  } catch (error) {
    console.error('찜하기 실패:', error);
    throw error; // 다시 던지기 (호출 쪽도 처리)
  }
}

// 찜 취소
export async function removeSaved(buildingId) {
  try {
    const { data } = await http.delete(`/api/member/saved/${buildingId}`);
    return data;
  } catch (error) {
    console.error('찜 취소 실패:', error);
    throw error;
  }
}

// 찜 목록 조회
export async function getSavedList() {
  try {
    const { data } = await http.get('/api/member/saved');
    return Array.isArray(data) ? data : [];
  } catch (error) {
    console.error('찜 목록 조회 실패:', error);
    throw error;
  }
}
