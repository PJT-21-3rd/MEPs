package org.meps.hjd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.hjd.dto.HjdNavigateResponseDto;

@Mapper
public interface HjdMapper {
    HjdNavigateResponseDto findByCoordinate(@Param("lat") double lat, @Param("lng") double lng);
}
