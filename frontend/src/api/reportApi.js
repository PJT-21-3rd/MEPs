import http from './axios';
import { getGradeByStatusCode, FACTOR_CODE_TO_KEY } from '@/constants/reportConstants';

// AI 안심 진단 리포트 조회 API (기능 6·7·8번)

const REPORT_ENDPOINT = (buildingId) => `/buildings/${buildingId}/safety-report/basic`;

// BE 응답을 컴포넌트가 쓰는 형태로 변환
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

/**
 * 건물 ID로 AI 안심 진단 리포트 조회
 * (GET /buildings/{buildingId}/safety-report/basic)
 * @param {string|number} buildingId
 * @returns {Promise<{score: number, grade: string, overallBriefing: string, dangerItems: object}>}
 * @throws
 */
export async function fetchReportData(buildingId) {
  const { data } = await http.get(REPORT_ENDPOINT(buildingId));
  return transformReportResponse(data);
}
