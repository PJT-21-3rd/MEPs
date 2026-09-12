package org.meps.fire.service;

import org.junit.jupiter.api.Test;
import org.meps.common.util.SafetyGrade;
import org.meps.fire.dto.FireScoreInput;
import org.meps.fire.dto.FireScoreResult;

import static org.assertj.core.api.Assertions.assertThat;

class FireScoreServiceTest {

    private final FireScoreService fireScoreService = new FireScoreService(null);

    private static FireScoreInput.FireScoreInputBuilder best() {
        // 전 축 최상 조건: 콘크리트 계열 + 광대로 + 평균 출동권 + 화재 최저 행정동
        return FireScoreInput.builder()
                .strctCdNm("철근콘크리트구조")
                .roadSideCodeNm("광대로한면")
                .stationNm("종로소방서")
                .stationDistanceM(500.0)
                .dongFireAvgCnt(3.0);
    }

    @Test
    void 전_축_최상이면_100점_안전이다() {
        FireScoreResult result = fireScoreService.calculateScore(best().build());

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.getDongFireQuartile()).isEqualTo(1);
    }

    @Test
    void 결측은_감점하지_않는다() {
        // 주구조·도로접면·거리·화재건수 전부 NULL → 정보 없음 = 중립, 100점
        FireScoreResult result = fireScoreService.calculateScore(FireScoreInput.builder().build());

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.getDongFireQuartile()).isNull();
    }

    @Test
    void 최악_조합이면_하한_70점으로_클램프된다() {
        // 13 + 18 + 9 + 5 = 45 > 클램프 폭 30 — 취약 조건이 겹치면 70점으로 수렴하는 설계 가드
        FireScoreResult result = fireScoreService.calculateScore(best()
                .strctCdNm("일반목구조")
                .roadSideCodeNm("맹지")
                .stationDistanceM(2500.0)
                .dongFireAvgCnt(80.3)
                .build());

        assertThat(result.getScore()).isEqualTo(70);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.CAUTION);
    }

    @Test
    void 목구조에_진입불가_접면이면_2축만으로_하한에_닿는다() {
        // 13 + 18 = 31 > 30 → 70점 (실측상 서울 4건 수준의 극단 조합)
        FireScoreResult result = fireScoreService.calculateScore(best()
                .strctCdNm("일반목구조")
                .roadSideCodeNm("세로한면(불)")
                .build());

        assertThat(result.getScore()).isEqualTo(70);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.CAUTION);
    }

    @Test
    void 조적조에_세로가_접면이면_양호_중단이다() {
        // 벽돌(7) + 세로(가)(10) = 17 → 83점 양호. 서울에서 가장 흔한 취약 조합의 위치 고정 —
        // 상수 튜닝 시 등급이 뒤집히기 쉬운 민감 지점 (2026-08-20 감점 확대 전에는 90 안전)
        FireScoreResult result = fireScoreService.calculateScore(best()
                .strctCdNm("벽돌구조")
                .roadSideCodeNm("세로한면(가)")
                .build());

        assertThat(result.getScore()).isEqualTo(83);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.GOOD);
    }

    @Test
    void 복합취약_세그먼트는_거리와_무관하게_주의이고_거리_감점이_겹치면_하한이다() {
        // 조적(7) + 세로(불)(18) = 25 → 75 주의. 골든타임 초과(9)가 겹치면 34 > 30 → 70.
        // 벽돌×세로(불) 573건이 경고 대상이 되는 경로
        int withoutDistance = fireScoreService.calculateScore(best()
                .strctCdNm("벽돌구조")
                .roadSideCodeNm("세로각지(불)")
                .build()).getScore();
        FireScoreResult withDistance = fireScoreService.calculateScore(best()
                .strctCdNm("벽돌구조")
                .roadSideCodeNm("세로각지(불)")
                .stationDistanceM(1900.0)
                .build());

        assertThat(withoutDistance).isEqualTo(75);
        assertThat(withDistance.getScore()).isEqualTo(70);
        assertThat(withDistance.getGrade()).isEqualTo(SafetyGrade.CAUTION);
    }

    @Test
    void 화재_다발_행정동이라는_이유만으로는_등급이_바뀌지_않는다() {
        // 지역 통계 단독 최대 감점 = 5 → 95 안전 (면책 문구 정합성 가드)
        FireScoreResult result = fireScoreService.calculateScore(best()
                .dongFireAvgCnt(80.3)
                .build());

        assertThat(result.getScore()).isEqualTo(95);
        assertThat(result.getGrade()).isEqualTo(SafetyGrade.SAFE);
        assertThat(result.getDongFireQuartile()).isEqualTo(4);
    }

    @Test
    void 주구조_등급표_콘크리트_계열은_감점이_없다() {
        assertThat(fireScoreService.calculateScore(best().strctCdNm("철골철근콘크리트구조").build())
                .getScore()).isEqualTo(100);
        assertThat(fireScoreService.calculateScore(best().strctCdNm("프리케스트콘크리트구조").build())
                .getScore()).isEqualTo(100);
    }

    @Test
    void 주구조_등급표_조적_철골_계열은_내화_불확정_감점이다() {
        // 피난·방화규칙 제3조: 벽돌조는 두께 19cm 이상일 때만 내화 인정 — 표제부만으론 불확정 → −7
        assertThat(fireScoreService.calculateScore(best().strctCdNm("벽돌구조").build())
                .getScore()).isEqualTo(93);
        assertThat(fireScoreService.calculateScore(best().strctCdNm("블록구조").build())
                .getScore()).isEqualTo(93);
        assertThat(fireScoreService.calculateScore(best().strctCdNm("일반철골구조").build())
                .getScore()).isEqualTo(93);
        assertThat(fireScoreService.calculateScore(best().strctCdNm("라멘조").build())
                .getScore()).isEqualTo(93);
    }

    @Test
    void 주구조_등급표_가연_구조는_최대_감점이다() {
        assertThat(fireScoreService.calculateScore(best().strctCdNm("일반목구조").build())
                .getScore()).isEqualTo(87);
        assertThat(fireScoreService.calculateScore(best().strctCdNm("경량철골구조").build())
                .getScore()).isEqualTo(87);
        assertThat(fireScoreService.calculateScore(best().strctCdNm("조립식판넬조").build())
                .getScore()).isEqualTo(87);
    }

    @Test
    void 도로접면_등급표_중로까지는_감점이_없고_소로부터_감점이_시작된다() {
        assertThat(fireScoreService.calculateScore(best().roadSideCodeNm("중로각지").build())
                .getScore()).isEqualTo(100);
        assertThat(fireScoreService.calculateScore(best().roadSideCodeNm("소로한면").build())
                .getScore()).isEqualTo(95);
        assertThat(fireScoreService.calculateScore(best().roadSideCodeNm("세로각지(가)").build())
                .getScore()).isEqualTo(90);
        assertThat(fireScoreService.calculateScore(best().roadSideCodeNm("지정되지않음").build())
                .getScore()).isEqualTo(100);
    }

    @Test
    void 경계값_소방서_거리는_평균_출동권과_골든타임_한계를_경계_포함으로_가른다() {
        // 1.2km 이하 0 / 1.8km 이하 −5 / 초과 −9
        assertThat(fireScoreService.calculateScore(best().stationDistanceM(1200.0).build())
                .getScore()).isEqualTo(100);
        assertThat(fireScoreService.calculateScore(best().stationDistanceM(1200.1).build())
                .getScore()).isEqualTo(95);
        assertThat(fireScoreService.calculateScore(best().stationDistanceM(1800.0).build())
                .getScore()).isEqualTo(95);
        assertThat(fireScoreService.calculateScore(best().stationDistanceM(1800.1).build())
                .getScore()).isEqualTo(91);
    }

    @Test
    void 경계값_화재_건수_사분위_경계에서_감점이_한_단계씩_오른다() {
        // Q1 8.0 / Q2 11.7 / Q3 16.7 — 각 사분위 구간의 최댓값, 이하면 포함 (2023~2025 실측 분포, 16.7~17.0 공백)
        // 사분위 감점 0/2/3/5
        assertThat(fireScoreService.calculateScore(best().dongFireAvgCnt(8.0).build())
                .getScore()).isEqualTo(100);
        assertThat(fireScoreService.calculateScore(best().dongFireAvgCnt(8.1).build())
                .getScore()).isEqualTo(98);
        assertThat(fireScoreService.calculateScore(best().dongFireAvgCnt(11.7).build())
                .getScore()).isEqualTo(98);
        assertThat(fireScoreService.calculateScore(best().dongFireAvgCnt(11.8).build())
                .getScore()).isEqualTo(97);
        assertThat(fireScoreService.calculateScore(best().dongFireAvgCnt(16.7).build())
                .getScore()).isEqualTo(97);
        assertThat(fireScoreService.calculateScore(best().dongFireAvgCnt(17.0).build())
                .getScore()).isEqualTo(95);
    }
}
