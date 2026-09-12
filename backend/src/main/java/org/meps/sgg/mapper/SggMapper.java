package org.meps.sgg.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.meps.sgg.dto.SggBriefingResponseDto;

@Mapper
public interface SggMapper {
    SggBriefingResponseDto findBySggCd(@Param("sggCd") String sggCd);
}
