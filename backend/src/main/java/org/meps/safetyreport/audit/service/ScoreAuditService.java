package org.meps.safetyreport.audit.service;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.meps.fire.dto.FireScoreInput;
import org.meps.fire.dto.FireScoreResult;
import org.meps.fire.service.FireScoreService;
import org.meps.flood.dto.FloodIncidentDto;
import org.meps.flood.dto.FloodScoreResultDto;
import org.meps.flood.service.FloodScoreService;
import org.meps.safetyreport.audit.dto.AuditBuildingFactsDto;
import org.meps.safetyreport.audit.dto.AuditDongFireAvgDto;
import org.meps.safetyreport.audit.dto.AuditFloodIncidentDto;
import org.meps.safetyreport.audit.dto.AuditSinkIncidentDto;
import org.meps.safetyreport.audit.dto.AuditStationDto;
import org.meps.safetyreport.audit.dto.ScoreAuditRowDto;
import org.meps.safetyreport.audit.mapper.ScoreAuditMapper;
import org.meps.safetyreport.service.TotalScoreService;
import org.meps.sinkhole.dto.SinkholeIncidentDto;
import org.meps.sinkhole.dto.SinkholeScoreResult;
import org.meps.sinkhole.service.SinkholeScoreService;
import org.meps.structure.dto.BuildingStructuralInfoDto;
import org.meps.structure.dto.StructuralStabilityScoreResultDto;
import org.meps.structure.service.StructuralStabilityScoreService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 안전점수 전수조사 배치.
 *
 * 운영 API와 동일한 점수를 보장하기 위해 각 축 서비스의 순수 계산부(calculateScore)를
 * 그대로 호출하고, 입력 사실만 전 건물 일괄 쿼리로 가져온다. 결과는 safety_score_audit에
 * 적재해 SQL로 분포·원인 분석을 할 수 있게 한다. 점수 로직을 바꾼 뒤 재실행하면
 * 변경 전후 분포 비교가 가능하다.
 *
 * 유일하게 계산 경로가 다른 부분은 최근접 소방서 거리다. 운영은 건물마다
 * ST_Distance_Sphere를 실행하지만 전수조사는 137개 소방서 좌표를 메모리에 두고
 * 같은 구면 공식(반지름 6,370,986m)으로 계산한다 — RDS에 4,800만 회 거리 연산을
 * 시키지 않기 위한 선택이고, 공식이 같아 결과 차이는 반올림 수준이다.
 */
@Service
@RequiredArgsConstructor
public class ScoreAuditService {

    private static final Logger log = LogManager.getLogger(ScoreAuditService.class);

    private static final int PAGE_SIZE = 20000;
    private static final int INSERT_CHUNK_SIZE = 1000;

    // MySQL ST_Distance_Sphere의 기본 지구 반지름(m)과 동일하게 맞춘다
    private static final double EARTH_RADIUS_M = 6370986.0;

    private final ScoreAuditMapper scoreAuditMapper;
    private final FireScoreService fireScoreService;
    private final StructuralStabilityScoreService structuralStabilityScoreService;
    private final SinkholeScoreService sinkholeScoreService;
    private final FloodScoreService floodScoreService;
    private final TotalScoreService totalScoreService;

    /** 전 건물 점수를 산출해 safety_score_audit에 적재하고 처리 건수를 반환한다 */
    public int runFullAudit() {
        List<AuditStationDto> stations = scoreAuditMapper.findAllStations();
        Map<String, Double> dongFireAvgMap = loadDongFireAverages();
        Map<String, List<SinkholeIncidentDto>> sinkMap = loadSinkIncidents();
        Map<String, List<FloodIncidentDto>> floodMap = loadFloodIncidents();
        LocalDate baseDate = LocalDate.now();

        log.info("전수조사 시작 — 소방서 {}곳, 화재통계 행정동 {}곳, 지반침하 보유 건물 {}동, 침수 보유 건물 {}동",
                stations.size(), dongFireAvgMap.size(), sinkMap.size(), floodMap.size());

        scoreAuditMapper.deleteAllAuditRows();

        int totalCount = 0;
        String lastId = "";
        while (true) {
            List<AuditBuildingFactsDto> page = scoreAuditMapper.findBuildingFactsPage(lastId, PAGE_SIZE);
            if (page.isEmpty()) {
                break;
            }

            List<ScoreAuditRowDto> rows = new ArrayList<>(page.size());
            for (AuditBuildingFactsDto facts : page) {
                rows.add(buildAuditRow(facts, stations, dongFireAvgMap, sinkMap, floodMap, baseDate));
            }
            insertInChunks(rows);

            totalCount += page.size();
            lastId = page.get(page.size() - 1).getBdMgtSn();
            log.info("전수조사 진행 — {}동 적재 완료", totalCount);
        }

        log.info("전수조사 종료 — 총 {}동", totalCount);
        return totalCount;
    }

    private ScoreAuditRowDto buildAuditRow(
            AuditBuildingFactsDto facts,
            List<AuditStationDto> stations,
            Map<String, Double> dongFireAvgMap,
            Map<String, List<SinkholeIncidentDto>> sinkMap,
            Map<String, List<FloodIncidentDto>> floodMap,
            LocalDate baseDate) {

        BuildingStructuralInfoDto structInfo = BuildingStructuralInfoDto.builder()
                .bdMgtSn(facts.getBdMgtSn())
                .useAprDay(facts.getUseAprDay())
                .strctCdNm(facts.getStrctCdNm())
                .violBdYn(facts.getViolBdYn())
                .ugrndFlr(facts.getUgrndFlr())
                .build();
        StructuralStabilityScoreResultDto struct = structuralStabilityScoreService.calculateScore(structInfo);

        Double stationDistanceM = nearestStationDistance(facts.getLat(), facts.getLng(), stations);
        FireScoreInput fireInput = FireScoreInput.builder()
                .strctCdNm(facts.getStrctCdNm())
                .roadSideCodeNm(facts.getRoadSideCodeNm())
                .stationDistanceM(stationDistanceM)
                .dongFireAvgCnt(dongFireAvgMap.get(facts.getHjdCd()))
                .build();
        FireScoreResult fire = fireScoreService.calculateScore(fireInput);

        List<SinkholeIncidentDto> sinkIncidents = sinkMap.get(facts.getBdMgtSn());
        if (sinkIncidents == null) {
            sinkIncidents = Collections.emptyList();
        }
        SinkholeScoreResult sink = sinkholeScoreService.calculateScore(sinkIncidents, baseDate);

        List<FloodIncidentDto> floodIncidents = floodMap.get(facts.getBdMgtSn());
        if (floodIncidents == null) {
            floodIncidents = Collections.emptyList();
        }
        FloodScoreResultDto flood = floodScoreService.calculateScore(floodIncidents, baseDate);

        int totalScore = totalScoreService.calculateTotalScore(
                struct.getScore(), fire.getScore(), sink.getScore(), flood.getScore());

        return ScoreAuditRowDto.builder()
                .bdMgtSn(facts.getBdMgtSn())
                .totalScore(totalScore)
                .structScore(struct.getScore())
                .fireScore(fire.getScore())
                .sinkScore(sink.getScore())
                .floodScore(flood.getScore())
                .useAprDay(facts.getUseAprDay())
                .strctCdNm(facts.getStrctCdNm())
                .violBdYn(facts.getViolBdYn())
                .ugrndFlr(facts.getUgrndFlr())
                .roadSideCodeNm(facts.getRoadSideCodeNm())
                .stationDistM(stationDistanceM)
                .dongFireAvg(fire.getDongFireAvgCnt())
                .dongFireQuartile(fire.getDongFireQuartile())
                .sinkCnt(sinkIncidents.size())
                .floodCnt(floodIncidents.size())
                .build();
    }

    private Map<String, Double> loadDongFireAverages() {
        List<AuditDongFireAvgDto> list = scoreAuditMapper.findDongFireAverages(
                FireScoreService.FIRE_STAT_FROM_YEAR, FireScoreService.FIRE_STAT_TO_YEAR);
        Map<String, Double> map = new HashMap<>();
        for (AuditDongFireAvgDto dto : list) {
            map.put(dto.getHjdCd(), dto.getAvgCnt());
        }
        return map;
    }

    private Map<String, List<SinkholeIncidentDto>> loadSinkIncidents() {
        List<AuditSinkIncidentDto> list = scoreAuditMapper.findAllSinkIncidents();
        Map<String, List<SinkholeIncidentDto>> map = new HashMap<>();
        for (AuditSinkIncidentDto dto : list) {
            List<SinkholeIncidentDto> incidents = map.get(dto.getBdMgtSn());
            if (incidents == null) {
                incidents = new ArrayList<>();
                map.put(dto.getBdMgtSn(), incidents);
            }
            incidents.add(SinkholeIncidentDto.builder()
                    .sagoDate(dto.getSagoDate())
                    .distanceM(dto.getDistanceM())
                    .build());
        }
        return map;
    }

    private Map<String, List<FloodIncidentDto>> loadFloodIncidents() {
        List<AuditFloodIncidentDto> list = scoreAuditMapper.findAllFloodIncidents();
        Map<String, List<FloodIncidentDto>> map = new HashMap<>();
        for (AuditFloodIncidentDto dto : list) {
            List<FloodIncidentDto> incidents = map.get(dto.getBdMgtSn());
            if (incidents == null) {
                incidents = new ArrayList<>();
                map.put(dto.getBdMgtSn(), incidents);
            }
            incidents.add(FloodIncidentDto.builder()
                    .year(dto.getYear())
                    .grade(dto.getGrade())
                    .build());
        }
        return map;
    }

    private Double nearestStationDistance(double lat, double lng, List<AuditStationDto> stations) {
        if (stations.isEmpty()) {
            return null;
        }
        double min = Double.MAX_VALUE;
        for (AuditStationDto station : stations) {
            double distance = sphereDistanceM(lat, lng, station.getLat(), station.getLng());
            if (distance < min) {
                min = distance;
            }
        }
        return min;
    }

    /** ST_Distance_Sphere와 같은 구면(하버사인) 거리 */
    private double sphereDistanceM(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }

    private void insertInChunks(List<ScoreAuditRowDto> rows) {
        int from = 0;
        while (from < rows.size()) {
            int to = Math.min(from + INSERT_CHUNK_SIZE, rows.size());
            scoreAuditMapper.insertAuditRows(rows.subList(from, to));
            from = to;
        }
    }
}
