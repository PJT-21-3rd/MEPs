package org.meps.structure.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.structure.dto.BuildingStructuralInfoDto;

@Mapper
public interface StructureMapper {

    BuildingStructuralInfoDto findStructuralInfo(@Param("buildingId") String buildingId);
}
