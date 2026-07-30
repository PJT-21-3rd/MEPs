package org.meps.hjd.service;


import lombok.RequiredArgsConstructor;
import org.meps.hjd.dto.HjdNavigateResponse;
import org.meps.hjd.exception.HjdNotFoundException;
import org.meps.hjd.mapper.HjdMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HjdService {

    private final HjdMapper hjdMapper;

    public HjdNavigateResponse findByCoordinate(double lat, double lng) {
        HjdNavigateResponse result = hjdMapper.findByCoordinate(lat, lng);
        if (result == null) {
            throw new HjdNotFoundException(lat, lng);
        }

        return result;
    }
}
