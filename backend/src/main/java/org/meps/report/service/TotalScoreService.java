package org.meps.report.service;

import org.springframework.stereotype.Service;

/**
 * 4개 안전 요소 점수(구조안정성/화재안정성/지반침하/침수이력)를 합산하여 종합 안전점수 산출
 *
 * 산식: total = round(Σ weight_i × score_i), Σweight_i = 1.0
 * - 입력이 모두 70~100 범위이고 가중치 합이 1.0이라 가중합도 자동으로 70~100 범위를 유지한다
 * - 가중치는 위험의 성격(영구적 결함 vs 발생형 이력)과 심각도 기준의 정책값:
 *   구조 35% — 건물 자체의 영구적 결함으로 붕괴 리스크의 근본 원인
 *   화재 30% — 인명과 직결되며 주구조·도로접면·소방서 거리·화재 통계를 합성한 복합 지표
 *   침수 20% — 반지하 등 실사고가 존재하는 반복형 환경 위험
 *   지반침하 15% — 발생 빈도는 가장 낮지만 심각도가 커 최소 비중은 유지
 */
@Service
public class TotalScoreService {

    private static final double WEIGHT_STRUCT = 0.35;
    private static final double WEIGHT_FIRE = 0.30;
    private static final double WEIGHT_FLOOD = 0.20;
    private static final double WEIGHT_SINK = 0.15;

    public int calculateTotalScore(int structScore, int fireScore, int sinkScore, int floodScore) {
        double weighted = structScore * WEIGHT_STRUCT
                + fireScore * WEIGHT_FIRE
                + sinkScore * WEIGHT_SINK
                + floodScore * WEIGHT_FLOOD;
        return (int) Math.round(weighted);
    }
}
