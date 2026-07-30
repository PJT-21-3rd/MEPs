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
