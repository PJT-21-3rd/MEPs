package collectors.buildings;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class BuildingCollector {

    private static final String BJD_FILTER = "11215";

    private static final double MIN_LON = 127.056, MIN_LAT = 37.525;
    private static final double MAX_LON = 127.112, MAX_LAT = 37.572;

    private static final int GRID_COLS = 12;
    private static final int GRID_ROWS = 12;

    private static final String JUSO_FILE = "/Users/home/Desktop/build_seoul.txt";

    private static final long API_DELAY = 100;

    public static void main(String[] args) throws InterruptedException {
        JusoBuildingIndex.load(JUSO_FILE);

        Map<String, List<JsonNode>> titleCache = new HashMap<>();
        Map<String, List<JsonNode>> floorCache = new HashMap<>();
        Map<String, JsonNode> landCache = new HashMap<>();
        Map<String, String> parcelCache = new HashMap<>();

        int ok = 0, noTitle = 0, noJuso = 0, skipped = 0, otherBjd = 0, failedPnu = 0;
        Set<String> processed = new HashSet<>();   // 타일 경계에 걸친 건물 중복 방지

        BuildingDao.open();
        try {
            Set<String> existing = BuildingDao.loadExistingKeys();

            double lonStep = (MAX_LON - MIN_LON) / GRID_COLS;
            double latStep = (MAX_LAT - MIN_LAT) / GRID_ROWS;
            int tileNo = 0;

            for (int r = 0; r < GRID_ROWS; r++) {
                for (int c = 0; c < GRID_COLS; c++) {
                    tileNo++;
                    String bbox = String.format("%.6f,%.6f,%.6f,%.6f",
                            MIN_LON + c * lonStep, MIN_LAT + r * latStep,
                            MIN_LON + (c + 1) * lonStep, MIN_LAT + (r + 1) * latStep);

                    List<JsonNode> buildings = BuildingWfsClient.fetch(bbox);
                    System.out.printf("[타일 %d/%d] WFS %d건 (%s)%n",
                            tileNo, GRID_COLS * GRID_ROWS, buildings.size(), bbox);

                    if (buildings.size() >= 1000) {
                        System.err.println("[경고] 타일이 WFS 상한(1000)에 도달 — 격자를 더 잘게 나눠야 함");
                    }

                    for (JsonNode b : buildings) {
                        String bdMgtSn = BuildingWfsClient.bdMgtSn(b);
                        String pnu = BuildingWfsClient.pnu(b);

                        // 대상 구/동이 아니면 제외 (API 호출 없음)
                        if (BJD_FILTER != null && !pnu.startsWith(BJD_FILTER)) {
                            otherBjd++;
                            continue;
                        }
                        // 타일 경계에 걸쳐 중복 조회된 건물
                        if (!processed.add(bdMgtSn)) continue;
                        // 이미 적재된 건물
                        if (existing.contains(bdMgtSn)) {
                            skipped++;
                            continue;
                        }

                        // ---- pnu 단위 조회 (캐시 미스 시에만 API 호출) ----
                        if (!titleCache.containsKey(pnu)) {
                            try {
                                titleCache.put(pnu, BrTitleClient.fetch(pnu));
                                Thread.sleep(API_DELAY);
                                floorCache.put(pnu, BrFloorClient.fetch(pnu));
                                Thread.sleep(API_DELAY);
                                landCache.put(pnu, LandCharClient.fetch(pnu));
                                parcelCache.put(pnu, ParcelWfsClient.fetchGeometry(pnu));
                            } catch (ApiLimitException e) {
                                throw e;   // 한도 초과는 즉시 전체 중단
                            } catch (RuntimeException e) {
                                // 일시 장애(타임아웃 등) — 이 필지만 포기하고 계속.
                                // DB에 안 들어가므로 다음 재실행 때 자동 재수집됨.
                                titleCache.remove(pnu);
                                floorCache.remove(pnu);
                                landCache.remove(pnu);
                                parcelCache.remove(pnu);
                                failedPnu++;
                                System.err.println("[필지 실패 - 건너뜀] pnu=" + pnu
                                        + " : " + e.getMessage());
                                continue;
                            }
                        }

                        // ---- 매칭 ----
                        JusoBuildingIndex.Row juso = JusoBuildingIndex.find(bdMgtSn);
                        if (juso == null) noJuso++;

                        JsonNode title = BrTitleClient.match(titleCache.get(pnu), juso);
                        if (title == null) {
                            noTitle++;
                            System.out.println("[표제부 미매칭] " + bdMgtSn + " (pnu=" + pnu
                                    + ", 표제부 " + titleCache.get(pnu).size() + "건"
                                    + ", juso " + (juso != null ? "O" : "X") + ")");
                            continue;
                        }

                        String floorInfo = BrFloorClient.toFloorInfoJson(
                                floorCache.get(pnu), BrTitleClient.mgmBldrgstPk(title));
                        JsonNode land = landCache.get(pnu);

                        // ---- 적재 ----
                        BuildingDao.add(
                                bdMgtSn,
                                pnu,
                                BuildingWfsClient.geometry(b),
                                BrTitleClient.roadAddr(title),
                                BrTitleClient.jibunAddr(title),
                                BrTitleClient.mainPurps(title),
                                BrTitleClient.bldNm(title),
                                BrTitleClient.grndFlr(title),
                                BrTitleClient.ugrndFlr(title),
                                BrTitleClient.useAprDay(title),
                                BrTitleClient.hoCnt(title),
                                BuildingWfsClient.violBdYn(b),
                                BrTitleClient.strctCdNm(title),
                                LandCharClient.lndcgrCodeNm(land),
                                LandCharClient.prposAreaNm(land),
                                LandCharClient.roadSideCodeNm(land),
                                LandCharClient.pblntfPclnd(land),
                                floorInfo,
                                parcelCache.get(pnu)
                        );
                        ok++;
                        if (ok % 50 == 0) {
                            System.out.println("... 적재 " + ok + "건 진행 중 (필지 "
                                    + titleCache.size() + "개 호출)");
                        }
                    }
                }
            }
        } catch (ApiLimitException e) {
            System.err.println();
            System.err.println("=== API 한도 초과로 중단 ===");
            System.err.println(e.getMessage());
            System.err.println("여기까지 적재된 분량은 커밋됩니다. 키 교체 또는 한도 갱신 후 재실행하면 이어서 진행됩니다.");
        } finally {
            BuildingDao.close();
        }

        System.out.println("---------------------------");
        System.out.println("적재 " + ok + "건 / 건너뜀 " + skipped
                + "건 / 표제부 미매칭 " + noTitle + "건 / juso 다리 없음 " + noJuso
                + "건 / 타동 제외 " + otherBjd + "건 / 필지 실패 " + failedPnu + "건");
        System.out.println("API 호출 필지 수: " + titleCache.size());
    }
}