package collectors.sggbriefing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class SggBriefingCalculator {

    /** 구 평균 건물연령과 총 건물 수(AI 브리핑 프롬프트 보강용) */
    public record BldAgeStat(BigDecimal avgBldAge, int bldCnt) {
    }

    private record AgeBracket(String label, int value) {
    }

    public SggBriefingLoader.Stat aggregate(
            SggBriefingExtractor.SggFlpopTotal flpop,
            Integer prevTotFlpop,
            SggBriefingExtractor.TopStore topStore,
            int targetDays,
            int prevDays
    ) {
        int dailyFlpop = toDailyAverage(flpop.totFlpop(), targetDays);

        AgeBracket majorBracket = findMajorAgeBracket(
                flpop.agrde10(), flpop.agrde20(), flpop.agrde30(),
                flpop.agrde40(), flpop.agrde50(), flpop.agrde60()
        );
        BigDecimal majorAgeRatio = ratioPercent(majorBracket.value(), flpop.totFlpop());

        BigDecimal flpopChgRate = null;
        if (prevTotFlpop != null) {
            int prevDailyFlpop = toDailyAverage(prevTotFlpop, prevDays);
            flpopChgRate = ratioPercent(dailyFlpop - prevDailyFlpop, prevDailyFlpop);
        }

        return new SggBriefingLoader.Stat(
                dailyFlpop,
                flpopChgRate,
                topStore == null ? null : topStore.indutyNm(),
                topStore == null ? null : topStore.storCnt(),
                majorBracket.label(),
                majorAgeRatio
        );
    }

    /** 행정동별 평균 건물연령을 그 행정동의 동수(bld_cnt)로 가중평균해 구 평균을 낸다 */
    public BldAgeStat computeBldAgeStat(List<SggBriefingExtractor.HjdStatRow> hjdStats) {
        int totalBldCnt = hjdStats.stream().mapToInt(SggBriefingExtractor.HjdStatRow::bldCnt).sum();
        if (totalBldCnt == 0) {
            return new BldAgeStat(null, 0);
        }

        BigDecimal weightedSum = BigDecimal.ZERO;
        for (SggBriefingExtractor.HjdStatRow row : hjdStats) {
            weightedSum = weightedSum.add(row.avgBldAge().multiply(BigDecimal.valueOf(row.bldCnt())));
        }
        BigDecimal avgBldAge = weightedSum.divide(BigDecimal.valueOf(totalBldCnt), 1, RoundingMode.HALF_UP);
        return new BldAgeStat(avgBldAge, totalBldCnt);
    }

    public int daysInQuarter(String yyqu) {
        int year = Integer.parseInt(yyqu.substring(0, 4));
        int quarter = Integer.parseInt(yyqu.substring(4, 5));
        int startMonth = (quarter - 1) * 3 + 1;
        LocalDate start = LocalDate.of(year, startMonth, 1);

        return (int) ChronoUnit.DAYS.between(start, start.plusMonths(3));
    }

    private AgeBracket findMajorAgeBracket(int a10, int a20, int a30, int a40, int a50, int a60) {
        return List.of(
                new AgeBracket("10대", a10), new AgeBracket("20대", a20), new AgeBracket("30대", a30),
                new AgeBracket("40대", a40), new AgeBracket("50대", a50), new AgeBracket("60대", a60)
        ).stream().max(Comparator.comparingInt(AgeBracket::value)).orElseThrow();
    }

    private int toDailyAverage(int totalValue, int days) {
        return BigDecimal.valueOf(totalValue)
                .divide(BigDecimal.valueOf(days), 0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    private BigDecimal ratioPercent(int numerator, int denominator) {
        if (denominator == 0) return null;
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
