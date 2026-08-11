// src/utils/formatters.js

/**
 * 숫자를 압축 표기법(만, 억 단위)으로 변환
 * @param {Number} num - 변환할 숫자
 * @returns {String} - 예: "12.5만"
 */
export const formatPopulation = (num) => {
  if (num === 0 || !num) return '0명';

  const formatter = new Intl.NumberFormat('ko-KR', {
    notation: 'compact',
    maximumFractionDigits: 1, // 소수점 첫째 자리까지만 표시
  });

  return `${formatter.format(num)}명`;
};

/**
 * 증감률 데이터를 텍스트로 변환
 * @param {Number} rate - 증감률 (예: 10.24 또는 -5.1)
 * @returns {String}
 */
export const formatRate = (rate) => {
  if (!rate || rate === 0) return '변동 없음';

  if (rate > 0) return `+${rate}%`;
  if (rate < 0) return `-${Math.abs(rate)}%`;
};

/**
 * YYYYMMDD 날짜 문자열을 YYYY-MM-DD 형태로 변환
 * @param {String} yyyymmdd - 8자리 날짜 문자열
 * @returns {String}
 */
export const formatDate = (yyyymmdd) => {
  if (!yyyymmdd || yyyymmdd.length !== 8) return yyyymmdd;
  return `${yyyymmdd.slice(0, 4)}-${yyyymmdd.slice(4, 6)}-${yyyymmdd.slice(6, 8)}`;
};

/**
 * 건축면적과 대지면적을 받아 건폐율(%)을 계산
 * 건폐율(%) = 건축면적 / 대지면적 × 100
 * @param {Number} archArea - 건축면적
 * @param {Number} platArea - 대지면적
 * @returns {Number|null} - 소수점 1자리 건폐율
 */
export const getBuildingCoverageRatio = (archArea, platArea) => {
  if (!archArea || !platArea) return null;
  return Math.round((archArea / platArea) * 1000) / 10;
};

/**
 * 건물 사용승인일(YYYYMMDD)을 기준으로 현재 몇 년 차인지 계산
 * @param {String} dateStr - 8자리 날짜 문자열 (useAprDay)
 * @returns {String} - 예: "준공 10년차"
 */
export const formatBuildingAge = (dateStr) => {
  if (!dateStr || dateStr.length < 4) return '연식 미상';

  const year = parseInt(dateStr.substring(0, 4));
  const currentYear = new Date().getFullYear();
  const age = currentYear - year + 1; // 당해 연도부터 1년차로 계산

  return `준공 ${age}년차`;
};

/**
 * 지상/지하 층수를 포맷팅하여 텍스트로 반환
 * @param {Number} up - 지상 층수 (grndFlr)
 * @param {Number} down - 지하 층수 (ugrndFlr)
 * @returns {String} - 예: "4F / B1" 또는 "층수 미상"
 */
export const formatFloor = (up = 0, down = 0) => {
  if (up === 0 && down === 0) return '-';

  let floorText = '';
  if (up > 0) floorText += `${up}F`;
  if (down > 0) floorText += `/B${down}`;

  return floorText.startsWith(' /') ? floorText.substring(3) : floorText;
};

/**
 * 지번/도로명 주소에서 앞의 '시/도' 부분을 제외하고 반환
 * @param {String} jibunAddr - 지번 주소
 * @param {String} roadAddr - 도로명 주소
 * @param {String} bldNm - 대체할 건물명 (주소가 없을 경우)
 * @returns {String} - 예: "광진구 화양동 212"
 */
export const formatShortAddress = (jibunAddr, roadAddr, bldNm) => {
  const rawAddr = jibunAddr || roadAddr;

  if (rawAddr) {
    const parts = rawAddr.split(' ');
    if (parts.length >= 2 && (parts[0].endsWith('시') || parts[0].endsWith('도'))) {
      return parts.slice(1).join(' '); // "서울특별시 광진구..." -> "광진구..."
    }
    return rawAddr;
  }

  return bldNm || '상세 주소 미상';
};

/**
 * 미터(m) 단위의 거리를 km 단위로 변환 (1000m 이상일 경우)
 * @param {Number} distance - 거리 (미터)
 * @returns {String} - 예: "1.2km" 또는 "120m"
 */
export const formatDistance = (distance) => {
  if (distance === undefined || distance === null) return '';

  if (distance >= 1000) {
    return (distance / 1000).toFixed(1) + 'km';
  }
  return distance + 'm';
};
