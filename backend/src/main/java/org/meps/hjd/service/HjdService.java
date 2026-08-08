package org.meps.hjd.service;


import lombok.RequiredArgsConstructor;
import org.meps.hjd.dto.HjdBboxDto;
import org.meps.hjd.dto.HjdBriefingResponseDto;
import org.meps.hjd.dto.HjdNamesDto;
import org.meps.hjd.dto.HjdNavigateResponseDto;
import org.meps.hjd.dto.SggBriefingResponseDto;
import org.meps.hjd.exception.AiBriefingNotAvailableException;
import org.meps.hjd.exception.HjdNotFoundException;
import org.meps.hjd.exception.SggNotFoundException;
import org.meps.hjd.mapper.HjdMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HjdService {

    private final HjdMapper hjdMapper;

    /** 서울 전체 행정동 리스트 조회 (시군구명·행정동명 순 정렬) */
    public HjdNamesDto getNames() {
        return HjdNamesDto.of(hjdMapper.findAllNames());
    }

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

    public HjdBriefingResponseDto getBriefing(String hjdCd) {
        HjdBriefingResponseDto briefing = hjdMapper.findByHjdCd(hjdCd);

        if (briefing == null) {
            throw new HjdNotFoundException(hjdCd);
        }

        if (briefing.getOverallBriefing() == null) {
            throw new AiBriefingNotAvailableException(hjdCd);
        }
        return briefing;
    }

    public SggBriefingResponseDto getSggBriefing(String sggCd) {
        SggBriefingResponseDto briefing = hjdMapper.findBySggCd(sggCd);

        if (briefing == null) {
            throw new SggNotFoundException(sggCd);
        }

        if (briefing.getOverallBriefing() == null) {
            throw new AiBriefingNotAvailableException("AI 브리핑이 아직 생성되지 않은 구입니다: " + sggCd);
        }
        return briefing;
    }
}
