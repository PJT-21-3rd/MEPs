package org.meps.hjd.service;


import lombok.RequiredArgsConstructor;
import org.meps.hjd.dto.HjdBboxDto;
import org.meps.hjd.dto.HjdNavigateResponseDto;
import org.meps.hjd.exception.HjdNotFoundException;
import org.meps.hjd.mapper.HjdMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HjdService {

    private final HjdMapper hjdMapper;

    public HjdNavigateResponseDto findByCoordinate(double lat, double lng) {
        HjdNavigateResponseDto result = hjdMapper.findByCoordinate(lat, lng);
        if (result == null) {
            throw new HjdNotFoundException(lat, lng);
        }

        return result;
    }

    /** 지역명(행정동 전방일치/시군구명)으로 경계 bbox 조회 - 시군구는 소속 행정동 합집합. 매칭 없으면 null */
    public HjdBboxDto findRegionBbox(String regionNm) {
        return hjdMapper.findRegionBboxByName(regionNm);
    }

    /** 좌표가 속한 행정동 조회. 없으면 null (404를 던지는 findByCoordinate와 달리 검색 폴백용) */
    public HjdNavigateResponseDto findRegionAt(double lat, double lng) {
        return hjdMapper.findByCoordinate(lat, lng);
    }

    /** 행정동 코드로 경계 bbox 조회. 없으면 null */
    public HjdBboxDto findBboxByHjdCd(String hjdCd) {
        return hjdMapper.findBboxByHjdCd(hjdCd);
    }
}
