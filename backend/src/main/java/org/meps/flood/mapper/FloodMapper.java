package org.meps.flood.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.flood.dto.FloodIncidentDto;

import java.util.List;

@Mapper
public interface FloodMapper {

    List<FloodIncidentDto> findIncidentsByBuilding(@Param("buildingId") String buildingId);
}
