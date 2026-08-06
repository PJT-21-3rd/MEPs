package org.meps.fire.service;

import lombok.RequiredArgsConstructor;
import org.meps.building.exception.BuildingNotFoundException;
import org.meps.fire.dto.FireFactsDto;
import org.meps.fire.dto.FireScoreInput;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.dto.FireStationDistanceDto;
import org.meps.fire.mapper.FireMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 화재안정성 점수(70~100) 계산.
 *
 * 산식: score = 100 − Σ감점, 감점 배분 = 주구조 9 / 도로접면 12 / 소방서 거리 6 / 행정동 화재 건수 3
 * - 합계 최대 30이라 하한 70이 설계상 보장된다
 *
 * 구간 경계 근거:
 * - 주구조: 건축물의 피난·방화구조 등의 기준에 관한 규칙 제3조 — 철근콘크리트조 등 콘크리트
 *   계열은 두께 10cm 이상이면 내화구조로 인정(상가 벽체는 사실상 자동 충족) → 감점 0.
 *   벽돌조·블록조는 두께 19cm 이상일 때만 인정 — 표제부 주구조명만으로 확인 불가(불확정) → −4.
 *   목구조·판넬조 등 인정 목록 외 가연·급속확산 구조 → −9
 * - 도로접면: 소방청 소방차 진입곤란·불가 구간 기준(기준 차량 중형펌프차 폭 2.5m).
 *   토지특성 세로(불) = "자동차 통행 불가능" = 펌프차 진입이 구조적으로 불가 → 맹지와 함께 −12.
 *   세로(가)는 폭 8m 미만 통행 가능 — 진입은 되나 사다리차 전개 등 활동 제약 → −6, 소로 −3
 * - 소방서 거리: 소방청 화재 골든타임 7분(최성기 8분 도달 이론). 서울 평균 출동거리 1.51km의
 *   직선 환산(우회계수 1.3) ≈ 1.2km 이내 = 평균 출동권 → 감점 0.
 *   골든타임 주행 가능 거리(7분 − 차고탈출 1분 11초, 도심 20~25km/h ≈ 도로 2.0~2.5km)의
 *   직선 환산 ≈ 1.8km 초과 = 골든타임 초과 우려 → −6. 전 건물 실측(평균 994m)상 약 3%만 해당
 * - 화재 건수: 절대 임계값에 공적 근거가 없어 서울 426개 행정동 분포 내 상대 위치(사분위)로
 *   구간화
 */
@Service
@RequiredArgsConstructor
public class FireScoreService {
    private static final int MAX_SCORE = 100;
    private static final int BASE_SCORE = 70;

    // 주구조 — 내화구조 인정 목록 외 가연·급속확산 구조 (표제부 주구조명 기준)
    private static final Set<String> STRUCT_VULNERABLE =
            Set.of("일반목구조", "통나무구조", "경량철골구조", "조립식판넬조", "기타구조");
    private static final String STRUCT_FIREPROOF_KEYWORD = "콘크리트"; // 콘크리트 계열 = 내화구조 자동 인정
    private static final int STRUCT_DEDUCT_UNCERTAIN = 4; // 조적·철골·재료 미상(라멘조 등) — 명칭만으로 내화 확인 불가
    private static final int STRUCT_DEDUCT_VULNERABLE = 9;

    // 도로접면 — 토지특성 접면 코드명 기준
    private static final String ROAD_UNSPECIFIED = "지정되지않음";
    private static final int ROAD_DEDUCT_SORO = 3; // 소로(8~12m)
    private static final int ROAD_DEDUCT_NARROW = 6; // 세로(가): 8m 미만 통행 가능 — 활동 제약
    private static final int ROAD_DEDUCT_NO_ENTRY = 12; // 세로(불)·맹지: 펌프차 진입 불가

    // 소방서 거리(직선, m)
    private static final double STATION_AVG_DISPATCH_M = 1200.0; // 서울 평균 출동거리 1.51km의 직선 환산
    private static final double STATION_GOLDEN_LIMIT_M = 1800.0; // 골든타임 7분 주행 한계의 직선 환산
    private static final int STATION_DEDUCT_MID = 3;
    private static final int STATION_DEDUCT_FAR = 6;

    // 행정동 화재 건수 — 2023~2025년 3개년 평균의 서울 426개 행정동 사분위 (연도 창과 세트로 갱신)
    private static final String FIRE_STAT_FROM_YEAR = "2023";
    private static final String FIRE_STAT_TO_YEAR = "2025";
    private static final double FIRE_CNT_Q1 = 8.0;
    private static final double FIRE_CNT_Q2 = 11.7;
    private static final double FIRE_CNT_Q3 = 16.7; // 각 경계는 해당 사분위 구간의 최댓값(이하 = 포함) — 실측상 16.7~17.0은 공백

    private final FireMapper fireMapper;

    public FireScoreResult getFireScore(String buildingId) {
        FireFactsDto facts = fireMapper.findBuildingFireFacts(buildingId);
        if (facts == null) {
            throw new BuildingNotFoundException(buildingId);
        }
        FireStationDistanceDto station = fireMapper.findNearestStation(buildingId);
        Double dongAvg = fireMapper.findDongAvgFireCnt(
                facts.getHjdCd(), FIRE_STAT_FROM_YEAR, FIRE_STAT_TO_YEAR);

        FireScoreInput input = FireScoreInput.builder()
                .strctCdNm(facts.getStrctCdNm())
                .roadSideCodeNm(facts.getRoadSideCodeNm())
                .stationNm(station == null ? null : station.getStationNm())
                .stationDistanceM(station == null ? null : station.getDistanceM())
                .dongFireAvgCnt(dongAvg)
                .build();
        return calculateScore(input);
    }

    /** 순수 계산부 — DB 없이 단위 테스트 가능 */
    public FireScoreResult calculateScore(FireScoreInput input) {
        Integer quartile = calculateDongFireQuartile(input.getDongFireAvgCnt());
        int totalDeduction = calculateStructureDeduction(input.getStrctCdNm())
                + calculateRoadDeduction(input.getRoadSideCodeNm())
                + calculateStationDeduction(input.getStationDistanceM())
                + calculateFireCountDeduction(quartile);
        int score = MAX_SCORE - totalDeduction;
        if (score < BASE_SCORE) {
            score = BASE_SCORE;
        }
        return FireScoreResult.of(score, input, quartile);
    }

    private int calculateStructureDeduction(String strctCdNm) {
        if (strctCdNm == null || strctCdNm.isBlank()) {
            return 0; // 정보 없음 = 중립
        }
        if (STRUCT_VULNERABLE.contains(strctCdNm)) {
            return STRUCT_DEDUCT_VULNERABLE;
        }
        if (strctCdNm.contains(STRUCT_FIREPROOF_KEYWORD)) {
            return 0;
        }
        return STRUCT_DEDUCT_UNCERTAIN;
    }

    private int calculateRoadDeduction(String roadSideCodeNm) {
        if (roadSideCodeNm == null || roadSideCodeNm.isBlank() || roadSideCodeNm.equals(ROAD_UNSPECIFIED)) {
            return 0; // 정보 없음 = 중립
        }
        if (roadSideCodeNm.equals("맹지")) {
            return ROAD_DEDUCT_NO_ENTRY;
        }
        if (roadSideCodeNm.startsWith("세로")) {
            if (roadSideCodeNm.contains("(불)")) {
                return ROAD_DEDUCT_NO_ENTRY;
            }
            return ROAD_DEDUCT_NARROW;
        }
        if (roadSideCodeNm.startsWith("소로")) {
            return ROAD_DEDUCT_SORO;
        }
        return 0; // 광대·중로
    }

    private int calculateStationDeduction(Double distanceM) {
        if (distanceM == null) {
            return 0; // 정보 없음 = 중립
        }
        if (distanceM <= STATION_AVG_DISPATCH_M) {
            return 0;
        }
        if (distanceM <= STATION_GOLDEN_LIMIT_M) {
            return STATION_DEDUCT_MID;
        }
        return STATION_DEDUCT_FAR;
    }

    private int calculateFireCountDeduction(Integer quartile) {
        if (quartile == null) {
            return 0; // 정보 없음 = 중립
        }
        return quartile - 1; // 1사분위 0 ~ 4사분위 −3
    }

    private Integer calculateDongFireQuartile(Double avgCnt) {
        if (avgCnt == null) {
            return null;
        }
        if (avgCnt <= FIRE_CNT_Q1) {
            return 1;
        }
        if (avgCnt <= FIRE_CNT_Q2) {
            return 2;
        }
        if (avgCnt <= FIRE_CNT_Q3) {
            return 3;
        }
        return 4;
    }
}
