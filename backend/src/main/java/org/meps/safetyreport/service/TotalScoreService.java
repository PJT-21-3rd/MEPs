package org.meps.safetyreport.service;

import org.springframework.stereotype.Service;

/**
 * 4개 안전 요소 점수(구조안정성/화재안정성/지반침하/침수이력)를 결합하여 종합 안전점수 산출
 *
 * 산식: total = max(70, round(100 − Σ mult_i × (100 − score_i)))
 * - 각 축의 결손(100 − score)을 계수배로 차감한다. 이전의 가중평균(Σweight = 1.0)은
 *   사고 이력이 없어 상시 100점인 축(지반침하 99.86%, 침수 99.88%)이 가중치만큼
 *   고정 가점으로 작동해, 나머지 축이 하한(70·70)이어도 종합 81점이 보장되는 문제가 있었다.
 *   계수 합을 1.0보다 크게(1.43) 두면 만점 축이 취약 축의 결손을 희석하지 못한다
 * - 계수 크기는 2026-08-20 전수조사(365,637동, 지반침하·침수 매핑 재적재 후) 분포를 기준으로
 *   주의(80점 미만) 약 20%, 최저점 70이 나오도록 보정한 정책값. 축 간 상대 비중
 *   (구조 > 화재 > 침수 > 지반침하)은 위험의 성격(영구적 결함 vs 발생형 이력)과
 *   심각도 기준의 기존 서열을 유지한다:
 *   구조 0.43 — 건물 자체의 영구적 결함으로 붕괴 리스크의 근본 원인
 *   화재 0.38 — 인명과 직결되며 주구조·도로접면·소방서 거리·화재 통계를 합성한 복합 지표
 *   침수 0.33 — 반지하 등 실사고가 존재하는 반복형 환경 위험
 *   지반침하 0.29 — 발생 빈도는 가장 낮지만 심각도가 커 최소 비중은 유지
 * - 축 점수가 모두 70~100 범위라 축별 결손 최대 30, 차감만으로는 하한이 100 − 30×1.43 = 57.1.
 *   축 점수와 같은 70~100 스케일을 유지하도록 70으로 클램프한다
 */
@Service
public class TotalScoreService {

    private static final int MIN_SCORE = 70;

    private static final double MULT_STRUCT = 0.43;
    private static final double MULT_FIRE = 0.38;
    private static final double MULT_FLOOD = 0.33;
    private static final double MULT_SINK = 0.29;

    public int calculateTotalScore(int structScore, int fireScore, int sinkScore, int floodScore) {
        double deficit = (100 - structScore) * MULT_STRUCT
                + (100 - fireScore) * MULT_FIRE
                + (100 - sinkScore) * MULT_SINK
                + (100 - floodScore) * MULT_FLOOD;
        return Math.max(MIN_SCORE, (int) Math.round(100 - deficit));
    }
}
