export const SCORE_GRADE_THRESHOLD = { WARNING: 70, GOOD: 80, SAFE: 90 }

export const GRADE_META = {
  danger: {
    label: '위험',
    text: 'text-status-danger',
    stroke: 'stroke-status-danger',
    badgeBg: 'bg-grade-danger',
  },
  warning: {
    label: '주의',
    text: 'text-text-warn',
    stroke: 'stroke-status-warn',
    badgeBg: 'bg-grade-warn',
  },
  good: {
    label: '양호',
    text: 'text-text-good',
    stroke: 'stroke-status-good',
    badgeBg: 'bg-grade-good',
  },
  safe: {
    label: '안전',
    text: 'text-text-safe',
    stroke: 'stroke-status-safe',
    badgeBg: 'bg-grade-safe',
  },
}

//  BE 응답의 status 코드(SAFE/GOOD/CAUTION/DANGER) -> 프론트 내부 grade 키(safe/good/warning/danger) 매핑

export const STATUS_CODE_TO_GRADE = {
  SAFE: 'safe',
  GOOD: 'good',
  CAUTION: 'warning',
  DANGER: 'danger',
}

export function getGradeByStatusCode(statusCode) {
  return STATUS_CODE_TO_GRADE[statusCode] ?? 'warning'
}

//  BE 응답의 factor 코드(STRUCTURE/FIRE/SINKHOLE/FLOOD) -> 프론트 내부 항목 키 매핑

export const FACTOR_CODE_TO_KEY = {
  STRUCTURE: 'structure',
  FIRE: 'fire',
  SINKHOLE: 'sinkhole',
  FLOOD: 'flood',
}

export function getGradeByScore(score) {
  if (score >= SCORE_GRADE_THRESHOLD.SAFE) return 'safe'
  if (score >= SCORE_GRADE_THRESHOLD.GOOD) return 'good'
  if (score >= SCORE_GRADE_THRESHOLD.WARNING) return 'warning'
  return 'danger'
}
