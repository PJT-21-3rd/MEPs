package org.meps.sgg.service;

import lombok.RequiredArgsConstructor;
import org.meps.hjd.exception.AiBriefingNotAvailableException;
import org.meps.sgg.dto.SggBriefingResponseDto;
import org.meps.sgg.exception.SggNotFoundException;
import org.meps.sgg.mapper.SggMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SggService {

    private final SggMapper sggMapper;

    public SggBriefingResponseDto getSggBriefing(String sggCd) {
        SggBriefingResponseDto briefing = sggMapper.findBySggCd(sggCd);

        if (briefing == null) {
            throw new SggNotFoundException(sggCd);
        }

        if (briefing.getOverallBriefing() == null) {
            throw new AiBriefingNotAvailableException("AI 브리핑이 아직 생성되지 않은 구입니다: " + sggCd);
        }
        return briefing;
    }
}
