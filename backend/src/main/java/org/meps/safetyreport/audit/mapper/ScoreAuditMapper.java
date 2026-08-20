package org.meps.safetyreport.audit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.safetyreport.audit.dto.AuditBuildingFactsDto;
import org.meps.safetyreport.audit.dto.AuditDongFireAvgDto;
import org.meps.safetyreport.audit.dto.AuditFloodIncidentDto;
import org.meps.safetyreport.audit.dto.AuditSinkIncidentDto;
import org.meps.safetyreport.audit.dto.AuditStationDto;
import org.meps.safetyreport.audit.dto.ScoreAuditRowDto;

import java.util.List;

/**
 * 안전점수 전수조사 배치 전용 매퍼.
 * 운영 API의 건물 단건 조회와 동일한 원천 사실을 전 건물 일괄(또는 키셋 페이지) 조회로 가져온다.
 */
@Mapper
public interface ScoreAuditMapper {

    List<AuditBuildingFactsDto> findBuildingFactsPage(
            @Param("lastId") String lastId, @Param("limit") int limit);

    List<AuditStationDto> findAllStations();

    List<AuditDongFireAvgDto> findDongFireAverages(
            @Param("fromYear") String fromYear, @Param("toYear") String toYear);

    List<AuditSinkIncidentDto> findAllSinkIncidents();

    List<AuditFloodIncidentDto> findAllFloodIncidents();

    void deleteAllAuditRows();

    void insertAuditRows(@Param("rows") List<ScoreAuditRowDto> rows);
}
