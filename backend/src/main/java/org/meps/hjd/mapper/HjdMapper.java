package org.meps.hjd.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.hjd.dto.HjdBboxDto;
import org.meps.hjd.dto.HjdBriefingResponseDto;
import org.meps.hjd.dto.HjdNameDto;
import org.meps.hjd.dto.HjdNavigateResponseDto;
import org.meps.hjd.dto.SggBriefingResponseDto;

import java.util.List;

@Mapper
public interface HjdMapper {
    List<HjdNameDto> findAllNames();

    HjdNavigateResponseDto findByCoordinate(@Param("lat") double lat, @Param("lng") double lng);

    HjdBboxDto findRegionBboxByName(@Param("keyword") String keyword);

    HjdBboxDto findBboxByHjdCd(@Param("hjdCd") String hjdCd);

    HjdBriefingResponseDto findByHjdCd(@Param("hjdCd") String hjdCd);

    SggBriefingResponseDto findBySggCd(@Param("sggCd") String sggCd);
}
