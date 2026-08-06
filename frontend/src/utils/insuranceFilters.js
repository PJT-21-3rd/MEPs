// 리포트 데이터의 insuranceRidersByFactor를 화면별로 필터링
// reportApi.js가 만든 원본 데이터를 각 모달이 필요로 하는 모양으로 가져다 씀

/**
 * 풍수해보험 모달용: flood 팩터의 PRODUCT 타입(정책보험 상품)만 추출
 * @param {object} insuranceRidersByFactor - reportData.insuranceRidersByFactor
 * @returns {Array<{coverageType: string, name: string, description: string}>}
 */
export function getFloodInsuranceProducts(insuranceRidersByFactor) {
  return (insuranceRidersByFactor?.flood ?? []).filter((item) => item.coverageType === 'PRODUCT');
}

/**
 * 사업장종합보험 모달용: 모든 팩터의 RIDER 타입(특약)을 전부 모음
 * @param {object} insuranceRidersByFactor - reportData.insuranceRidersByFactor
 * @returns {Array<{coverageType: string, name: string, description: string}>}
 */
export function getBusinessInsuranceRiders(insuranceRidersByFactor) {
  const all = [];
  Object.values(insuranceRidersByFactor ?? {}).forEach((list) => {
    all.push(...list.filter((item) => item.coverageType === 'RIDER'));
  });
  return all;
}

/**
 * 사업장종합보험 모달 맨 위에 표시할 "필수" 의무보험 1건
 * disasterLiability/fireLiability 중 required=true인 것의 첫 번째를 반환
 * @param {object} disasterLiability - { required, description, evidenceTags }
 * @param {object} fireLiability - { required, description, evidenceTags }
 * @returns {{name: string, description: string, badge: string} | null}
 */
export function getRequiredMandatoryInsurance(disasterLiability, fireLiability) {
  if (disasterLiability?.required) {
    return {
      name: '재난배상책임보험',
      description: disasterLiability.description,
      badge: '필수',
    };
  }
  if (fireLiability?.required) {
    return {
      name: '화재배상책임보험',
      description: fireLiability.description,
      badge: '필수',
    };
  }
  return null;
}
