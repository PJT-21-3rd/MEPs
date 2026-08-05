package collectors.hjdbriefing;

import collectors.common.Db;
import java.sql.*;
import java.util.*;

public class HjdBriefingStatCollector {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        HjdBriefingLoader.UpsertResult result = null;

        HjdBriefingExtractor extractor = new HjdBriefingExtractor();
        HjdBriefingCalculator calculator = new HjdBriefingCalculator();
        HjdBriefingLoader loader = new HjdBriefingLoader();

        try (Connection conn = Db.connect()) {
            conn.setAutoCommit(false);

            String targetYyqu = extractor.resolveTargetYyqu(conn);
            String prevYyqu = extractor.resolvePrevYyqu(conn, targetYyqu);
            int targetDays = calculator.daysInQuarter(targetYyqu);
            int prevDays = prevYyqu != null ? calculator.daysInQuarter(prevYyqu) : 0;

            List<HjdBriefingExtractor.FlpopRow> flpopRows = extractor.fetchFlpopRows(conn, targetYyqu);
            Map<String, Integer> prevTotFlpopByHjd = extractor.fetchTotFlpop(conn, prevYyqu);
            Map<String, HjdBriefingExtractor.StoreStat> storeStats = extractor.fetchTopStore(conn, targetYyqu);
            Map<String, HjdBriefingLoader.Stat> existingStats = loader.loadExisting(conn);

            // hjd_flpop 데이터가 있는 동을 기준으로 순회, hjd_store 쪽은 없으면 null 처리 (LEFT JOIN과 동일한 의미)
            Map<String, HjdBriefingLoader.Stat> newStats = new HashMap<>();
            for (HjdBriefingExtractor.FlpopRow row : flpopRows) {
                HjdBriefingCalculator.FlpopStat fp = calculator.computeFlpopStat(
                        row, prevTotFlpopByHjd.get(row.hjdCd()), targetDays, prevDays);
                HjdBriefingExtractor.StoreStat sp = storeStats.get(row.hjdCd());

                newStats.put(row.hjdCd(), new HjdBriefingLoader.Stat(
                        fp.dailyFlpop(),
                        fp.flpopChgRate(),
                        sp != null ? sp.topIndutyNm() : null,
                        sp != null ? sp.topIndutyStorCnt() : null,
                        fp.majorAgeGrp(),
                        fp.majorAgeRatio()
                ));
            }

            result = loader.upsertAll(conn, newStats, existingStats);
            conn.commit();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        System.out.println("=========================================");
        if (result != null) {
            System.out.printf("행정동 AI 브리핑 통계 집계 완료 (대상: %d개, ai_brf 리셋: %d개, 소요시간: %.2f초)%n",
                    result.upsertCount(), result.resetCount(), (endTime - startTime) / 1000.0);
        } else {
            System.out.println("행정동 AI 브리핑 통계 집계 실패");
        }
        System.out.println("=========================================");
    }
}