package org.meps.hjd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.hjd.dto.HjdNavigateResponse;

@Mapper
public interface HjdMapper {
    HjdNavigateResponse findByCoordinate(@Param("lat") double lat, @Param("lng") double lng);
}
