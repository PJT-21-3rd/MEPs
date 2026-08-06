import http from './axios';
import { getGradeByStatusCode, FACTOR_CODE_TO_KEY } from '@/constants/reportConstants';

// AI 안심 진단 리포트 조회 API (기능 6·7·8번)

const REPORT_ENDPOINT = (buildingId) => `/buildings/${buildingId}/safety-report/basic`;
// TODO: 정확한 엔드포인트 경로는 BE API 문서 확인 후 수정 필요
const DETAILED_REPORT_ENDPOINT = (buildingId) => `/buildings/${buildingId}/safety-report/detailed`;
const INSURANCE_RIDERS_ENDPOINT = '/insurance/riders';

function transformReportResponse(raw) {
  const dangerItems = {};

  raw.factors.forEach((factor) => {
    const key = FACTOR_CODE_TO_KEY[factor.code];
    if (!key) {
      // 매핑 안 된 factor 코드 로그
      console.warn(`[reportApi] Unknown factor code: ${factor.code}`);
      return;
    }

    dangerItems[key] = {
      status: getGradeByStatusCode(factor.status), // 내부 키로 변환
      summary: factor.briefing,
    };
  });

  return {
    score: raw.safetyScore,
    grade: getGradeByStatusCode(raw.overallStatus),
    overallBriefing: raw.overallBriefing,
    dangerItems,
  };
}

// 상세 리포트 응답 변환
// factors[i].aiReport: 팩터별 3단락(근거 데이터 설명/리스크/솔루션) 문자열
// factors[i].details: api 추후 매핑 예정
function transformDetailedReportResponse(raw) {
  const dangerItems = {};

  raw.factors.forEach((factor) => {
    const key = FACTOR_CODE_TO_KEY[factor.code];
    if (!key) {
      console.warn(`[reportApi] Unknown factor code: ${factor.code}`);
      return;
    }

    dangerItems[key] = {
      status: getGradeByStatusCode(factor.status),
      aiReport: factor.aiReport,
    };
  });

  return {
    overallAiReport: raw.overallAiReport,
    dangerItems,
  };
}

// 주의 판정된 factor 코드만 추출
function getCautionFactorCodes(rawFactors) {
  return rawFactors.filter((factor) => factor.status === 'CAUTION').map((factor) => factor.code);
}

// 특약/상품 응답 factorCode 기준으로 그룹핑
// 한 팩터에 RIDER/PRODUCT 여러 개 존재 가능 -> 배열로 묶음
function groupInsuranceRidersByFactor(items) {
  const grouped = {};

  items.forEach((item) => {
    const key = FACTOR_CODE_TO_KEY[item.factorCode];
    if (!key) return;

    if (!grouped[key]) grouped[key] = [];
    grouped[key].push({
      coverageType: item.coverageType,
      name: item.name,
      description: item.coverageSummary,
    });
  });

  return grouped;
}
/**
 * 건물 ID로 AI 안심 진단 리포트 조회
 * (GET /buildings/{buildingId}/safety-report/basic)
 * @param {string|number} buildingId
 * @returns {Promise<{score: number, grade: string, overallBriefing: string, dangerItems: object}>}
 * @throws
 */
export async function fetchReportData(buildingId) {
  const { data } = await http.get(REPORT_ENDPOINT(buildingId));
  const result = transformReportResponse(data);

  const cautionCodes = getCautionFactorCodes(data.factors);
  result.insuranceRidersByFactor = {};

  if (cautionCodes.length > 0) {
    try {
      const { data: coverageData } = await http.get(INSURANCE_RIDERS_ENDPOINT, {
        params: { factors: cautionCodes.join(',') },
      });
      result.insuranceRidersByFactor = groupInsuranceRidersByFactor(coverageData.items);

      // 특약 카드(#25/#32 진단 카드)용: 팩터별 RIDER 하나만 뽑아 detail.insurance로 병합
      Object.keys(result.insuranceRidersByFactor).forEach((key) => {
        const rider = result.insuranceRidersByFactor[key].find((i) => i.coverageType === 'RIDER');
        if (rider && result.dangerItems[key]) {
          result.dangerItems[key].detail = { insurance: rider };
        }
      });
    } catch (err) {
      console.warn('[reportApi] 특약/상품 목록 조회 실패', err);
    }
  }

  return result;
}

/**
 * 건물 ID로 4대 근거 전체 상세 진단 리포트 조회
 * (basic과 별도 엔드포인트 — 상세보기 클릭 시점에 호출)
 * @param {string|number} buildingId
 * @returns {Promise<{overallAiReport: string, dangerItems: object}>}
 * @throws
 */
export async function fetchDetailedReportData(buildingId) {
  const { data } = await http.get(DETAILED_REPORT_ENDPOINT(buildingId));
  return transformDetailedReportResponse(data);
}
