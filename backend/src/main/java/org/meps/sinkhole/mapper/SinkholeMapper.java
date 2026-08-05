package org.meps.sinkhole.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.sinkhole.dto.SinkholeIncidentDto;

import java.util.List;

@Mapper
public interface SinkholeMapper {

    List<SinkholeIncidentDto> findIncidentsByBuilding(
            @Param("buildingId") String buildingId
    );
}
