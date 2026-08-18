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
 * 사업장종합보험 모달 맨 위에 표시할 의무보험 2건 고정 노출
 * API 판별 없이 재난배상책임보험/화재배상책임보험 둘 다 항상 "필수" 배지로 표시
 * @param {object} disasterLiability - { description, evidenceTags }
 * @param {object} fireLiability - { description, evidenceTags }
 * @returns {Array<{name: string, description: string, badge: string}>}
 */
export function getMandatoryInsuranceItems(disasterLiability, fireLiability) {
  return [
    {
      name: '재난배상책임보험',
      description:
        disasterLiability?.description ?? '재난 및 안전관리 기본법에 따른 의무 가입 대상입니다.',
      badge: '필수',
    },
    {
      name: '화재배상책임보험',
      description:
        fireLiability?.description ??
        '화재로 인한 재해보상과 손해배상을 위한 의무 가입 대상입니다.',
      badge: '필수',
    },
  ];
}
