import http from '.';

// KB 대출 상품 조회 API (기능 12번)
const LOAN_PRODUCTS_ENDPOINT = '/api/loans';

// 상품별 상세 페이지 URL — API에 없는 필드라 프론트에서 loanName 기준으로 매칭
const LOAN_DETAIL_URLS = {
  'KB사장님+ 마이너스통장': 'https://zloan.kbstar.com/quics?page=C110940',
  'KB소상공인 신용대출': 'https://zloan.kbstar.com/quics?page=C106666',
  'KB소상공인 보증서대출(온택트)': 'http://zloan.kbstar.com/quics?page=C109681',
  '소상공인 저금리 대환대출(수탁보증)':
    'https://obiz.kbstar.com/quics?page=C016280&%EB%85%B8%EB%93%9C%EC%BD%94%EB%93%9C=00020&%EB%B8%8C%EB%9E%9C%EB%93%9C%EC%83%81%ED%92%88%EC%BD%94%EB%93%9C=LN25001435&cc=b035196%3Ab035393#C064396',
};
const INSURANCE_RIDERS_ENDPOINT = '/api/insurances';

// BE 응답(loanType/loanName/coverageSummary/minRate/maxLimit)을
// LoanModal.vue가 기대하는 필드(category/name/description/rateText)로 변환
function transformLoanResponse(raw) {
  return raw.items.map((item) => ({
    category: item.loanType,
    name: item.loanName,
    description: item.coverageSummary,
    rateText: `연 최저 ${item.minRate}%~`,
    maxLimit: item.maxLimit,
    detailUrl: LOAN_DETAIL_URLS[item.loanName] ?? null,
  }));
}

/**
 * KB 대출 상품 조회
 * (GET /api/loans)
 * @returns {Promise<Array<{category: string, name: string, description: string, rateText: string, maxLimit: number, detailUrl: string|null}>>}
 * @throws
 */
export async function fetchLoanProducts() {
  const { data } = await http.get(LOAN_PRODUCTS_ENDPOINT);
  return transformLoanResponse(data);
}

/**
 * 특정 팩터의 특약/상품 목록 직접 조회 (진단 등급과 무관하게 호출 가능)
 * @param {string} factorCode - 'FLOOD' 등
 * @returns {Promise<Array<{coverageType: string, name: string, description: string}>>}
 * @throws
 */
export async function fetchInsuranceRidersByFactor(factorCode) {
  const { data } = await http.get(INSURANCE_RIDERS_ENDPOINT, {
    params: { factors: factorCode },
  });
  return (data.items ?? []).map((item) => ({
    coverageType: item.coverageType,
    name: item.name,
    description: item.coverageSummary,
  }));
}
