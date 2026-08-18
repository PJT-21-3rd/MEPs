import http from '.';

// 찜하기
export async function addSaved(buildingId) {
  const { data } = await http.post(`/api/member/saved/${buildingId}`);
  return data;
}

// 찜 취소
export async function removeSaved(buildingId) {
  const { data } = await http.delete(`/api/member/saved/${buildingId}`);
  return data;
}

export async function getSavedList() {
  const { data } = await http.get('/api/member/saved');
  return Array.isArray(data) ? data : [];
}
