package org.meps.fire.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.fire.dto.FireFactsDto;
import org.meps.fire.dto.FireStationDistanceDto;

@Mapper
public interface FireMapper {

    FireFactsDto findBuildingFireFacts(
            @Param("buildingId") String buildingId
    );

    FireStationDistanceDto findNearestStation(
            @Param("buildingId") String buildingId
    );

    Double findDongAvgFireCnt(
            @Param("hjdCd") String hjdCd,
            @Param("fromYear") String fromYear,
            @Param("toYear") String toYear
    );
}
