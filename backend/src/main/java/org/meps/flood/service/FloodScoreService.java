package org.meps.flood.service;

import lombok.RequiredArgsConstructor;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.mapper.FloodMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

/**
 * 침수이력 안전점수 계산
 *
 * 산식: score = 70 + round(30 × RISK_DECAY^Σw), w = gradeWeight × recencyMultiplier
 * - 침수 이력 0건이면 100점
 * - 이력이 추가될 때마다 남은 안전 마진(score−70)이 곱으로 깎이고 마진이 음수가 될 수 없어 70점 하한이 자연히 보장된다
 * - 주요 기준은 등급 × 빈도이다. recency는 보조 가중치로만 둔다.
 *   과거(old) 이력을 기준(×1.0)으로 하고 최근 이력에만 소폭 가산(×(1+RECENCY_BONUS))한다.
 *   원본 데이터가 특정 해에만 몰려 있어 recent/old를 큰 배율 차이로 두면 특정 연도가 임계값을
 *   넘는 시점에 그 해 이력을 가진 모든 건물의 점수가 동시에 계단식으로 뛰는 문제가 있기 때문에 가중치를 작게 두었다.
 *   RISK_DECAY 0.27 = "최고등급(5등급) 이력 1건(과거, 기준선) = 78점(주의 구간 상단)" 정책 앵커에서 역산
 */
@Service
@RequiredArgsConstructor
public class FloodScoreService {
    private static final int BASE_SCORE = 70;
    private static final int SCORE_RANGE = 30;
    private static final double RISK_DECAY = 0.27;

    // 침수심 등급이 높을수록 가중치가 큼
    private static final Map<Integer, Double> GRADE_WEIGHTS = Map.of(
            1, 0.2,
            2, 0.4,
            3, 0.6,
            4, 0.8,
            5, 1.0
    );

    // recency는 보조 가중치(±10%)로 활용
    private static final int RECENT_CYCLE_YEARS = 5;
    private static final double RECENCY_BONUS = 0.1;

    private final FloodMapper floodMapper;

    public FloodScoreResultDto getFloodScore(String buildingId) {
        List<FloodIncidentDto> incidents = floodMapper.findIncidentsByBuilding(buildingId);
        return calculateScore(incidents, LocalDate.now());
    }

    public FloodScoreResultDto calculateScore(List<FloodIncidentDto> incidents, LocalDate baseDate) {
        double weightSum = 0.0;
        for (FloodIncidentDto incident : incidents) {
            weightSum += gradeWeight(incident.getGrade())
                    * recencyWeight(incident.getYear(), baseDate);
        }
        int score = BASE_SCORE + (int) Math.round(SCORE_RANGE * Math.pow(RISK_DECAY, weightSum));
        return FloodScoreResultDto.of(score, incidents);
    }

    private double gradeWeight(int grade) {
        Double weight = GRADE_WEIGHTS.get(grade);
        if (weight == null) {
            throw new IllegalStateException("Unexpected flood grade: " + grade);
        }
        return weight;
    }

    private double recencyWeight(String year, LocalDate baseDate) {
        int floodYear = Integer.parseInt(year);
        if (floodYear >= baseDate.getYear() - RECENT_CYCLE_YEARS) {
            return 1.0 + RECENCY_BONUS;
        }
        return 1.0;
    }
}
