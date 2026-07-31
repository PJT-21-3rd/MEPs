package org.meps.building.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.building.dto.NearbyBuildingDto;

import java.util.List;

@Mapper
public interface BuildingMapper {

    List<NearbyBuildingDto> findBuildingsInBounds(
            @Param("swLat") double swLat,
            @Param("swLng") double swLng,
            @Param("neLat") double neLat,
            @Param("neLng") double neLng,
            @Param("limit") int limit
    );
}