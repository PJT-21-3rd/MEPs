package collectors.hjdbriefing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class HjdBriefingCalculator {

    /** 계산된 유동인구 통계 (일평균, 증감률, 주요 연령대, 연령대 비율) */
    public record FlpopStat(
            int dailyFlpop,
            BigDecimal flpopChgRate,
            String majorAgeGrp,
            BigDecimal majorAgeRatio
    ) {
    }


    private record AgeBracket(String label, int value) {
    }

    /**
     * @param row          해당 분기 원본 데이터
     * @param prevTotFlpop 직전 분기 총 유동인구(raw). 직전 분기 데이터가 없으면 null → flpopChgRate도 null
     * @param targetDays   해당 분기의 실제 날짜 수
     * @param prevDays     직전 분기의 실제 날짜 수 (prevTotFlpop이 null이면 사용되지 않음)
     */
    public FlpopStat computeFlpopStat(
            HjdBriefingExtractor.FlpopRow row, Integer prevTotFlpop, int targetDays, int prevDays
    ) {
        int dailyFlpop = toDailyAverage(row.totFlpop(), targetDays);

        AgeBracket majorBracket = findMajorAgeBracket(
                row.agrde10(), row.agrde20(), row.agrde30(), row.agrde40(), row.agrde50(), row.agrde60()
        );

        BigDecimal majorAgeRatio = ratioPercent(majorBracket.value(), row.totFlpop());

        BigDecimal flpopChgRate = null;
        if (prevTotFlpop != null) {
            int prevDailyFlpop = toDailyAverage(prevTotFlpop, prevDays);
            flpopChgRate = ratioPercent(dailyFlpop - prevDailyFlpop, prevDailyFlpop);
        }

        return new FlpopStat(dailyFlpop, flpopChgRate, majorBracket.label(), majorAgeRatio);
    }

    private AgeBracket findMajorAgeBracket(int a10, int a20, int a30, int a40, int a50, int a60) {
        return List.of(
                new AgeBracket("10대", a10), new AgeBracket("20대", a20), new AgeBracket("30대", a30),
                new AgeBracket("40대", a40), new AgeBracket("50대", a50), new AgeBracket("60대", a60)
        ).stream().max(Comparator.comparingInt(AgeBracket::value)).orElseThrow();
    }

    public int daysInQuarter(String yyqu) {
        int year = Integer.parseInt(yyqu.substring(0, 4));
        int quarter = Integer.parseInt(yyqu.substring(4, 5));
        int startMonth = (quarter - 1) * 3 + 1;
        LocalDate start = LocalDate.of(year, startMonth, 1);

        return (int) ChronoUnit.DAYS.between(start, start.plusMonths(3));
    }

    private int toDailyAverage(int totalValue, int days) {
        return BigDecimal.valueOf(totalValue)
                .divide(BigDecimal.valueOf(days), 0, RoundingMode.HALF_UP)
                .intValueExact();
    }

    /** (numerator / denominator) * 100, 소수 둘째 자리 반올림. denominator가 0이면 null. */
    private BigDecimal ratioPercent(int numerator, int denominator) {
        if (denominator == 0) return null;
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
