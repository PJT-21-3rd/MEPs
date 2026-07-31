package org.meps.hjd.service;


import lombok.RequiredArgsConstructor;
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
}
