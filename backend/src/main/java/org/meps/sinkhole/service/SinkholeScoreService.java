package org.meps.sinkhole.service;

import lombok.RequiredArgsConstructor;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.sinkhole.mapper.SinkholeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 지반침하안정성 점수(70~100) 계산.
 *
 * 산식: score = 70 + round(30 × RISK_DECAY^Σw), w = w_dist × w_recency
 * - 사고 0건이면 100점. 사고가 추가될 때마다 남은 안전 마진(score−70)이 곱으로 깎여
 *   건수에 따라 단조 감소하고, 마진이 음수가 될 수 없어 70점 하한이 자연히 보장된다
 * - 구간 경계·가중치는 튜닝 가능한 정책값 (상수 참조). 근거:
 *   100m = 공학적 침하 영향권(굴착깊이 2~4배, 서울시 관측망 감지반경 50m) + 지오코딩 오차 버퍼,
 *   500m = 상세 리포트 명세의 사실 표시 반경 — 위험 반경이 아니므로 가중치 최소,
 *   5년 = 지하안전법 GPR 공동조사 주기 — 경과 시 주의→양호 승급의 출구,
 *   RISK_DECAY 0.27 = "근거리·최근 1건 = 78점(주의 구간 상단)" 정책 앵커에서 역산
 */
@Service
@RequiredArgsConstructor
public class SinkholeScoreService {
    private static final int BASE_SCORE = 70;
    private static final int SCORE_RANGE = 30;

    // 가중치 합 1.0당 남은 안전 마진(score−70)을 27%로 줄이는 감쇠율
    private static final double RISK_DECAY = 0.27;

    private static final double DIST_NEAR_M = 100.0;
    private static final double DIST_MID_M = 300.0;
    private static final double W_DIST_NEAR = 1.0; // 직접 영향권
    private static final double W_DIST_MID = 0.5; // 간접 신호(동일 노후 관로망·공사 영향권 공유 개연성)
    private static final double W_DIST_FAR = 0.2; // 표시 범위(매핑 배치가 500m로 컷하므로 그 외 = 300~500m)

    private static final int SURVEY_CYCLE_YEARS = 5; // 법정 GPR 공동조사 주기
    private static final double W_RECENT = 1.0;
    private static final double W_OLD = 0.6;

    private final SinkholeMapper sinkholeMapper;

    public SinkholeScoreResult getSinkholeScore(String buildingId) {
        List<SinkholeIncidentDto> incidents = sinkholeMapper.findIncidentsByBuilding(buildingId);
        return calculateScore(incidents, LocalDate.now());
    }

    /** 순수 계산부 — 기준일을 주입받아 DB 없이 단위 테스트 가능 */
    public SinkholeScoreResult calculateScore(List<SinkholeIncidentDto> incidents, LocalDate baseDate) {
        double weightSum = 0.0;
        for (SinkholeIncidentDto incident : incidents) {
            weightSum += distanceWeight(incident.getDistanceM())
                    * recencyWeight(incident.getSagoDate(), baseDate);
        }
        int score = BASE_SCORE + (int) Math.round(SCORE_RANGE * Math.pow(RISK_DECAY, weightSum));
        return SinkholeScoreResult.of(score, incidents);
    }

    private double distanceWeight(double distanceM) {
        if (distanceM <= DIST_NEAR_M) {
            return W_DIST_NEAR;
        }
        if (distanceM <= DIST_MID_M) {
            return W_DIST_MID;
        }
        return W_DIST_FAR;
    }

    private double recencyWeight(String sagoDate, LocalDate baseDate) {
        LocalDate sago = LocalDate.parse(sagoDate, DateTimeFormatter.BASIC_ISO_DATE);
        if (!sago.isBefore(baseDate.minusYears(SURVEY_CYCLE_YEARS))) {
            return W_RECENT;
        }
        return W_OLD;
    }
}
